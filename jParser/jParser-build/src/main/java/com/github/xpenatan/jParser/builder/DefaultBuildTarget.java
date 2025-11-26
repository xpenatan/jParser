package com.github.xpenatan.jParser.builder;

import com.github.xpenatan.jParser.core.JParser;
import com.github.xpenatan.jParser.core.util.CustomFileDescriptor;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public abstract class DefaultBuildTarget extends BuildTarget {

    private static String helperNameH = "IDLHelper.h";
    private static String helperNameCpp = "IDLHelper.cpp";

    public boolean multiCoreCompile = true;

    public String tempBuildDir;

    public final ArrayList<String> additionalSourceDirs = new ArrayList<>();
    private final ArrayList<String> compilerCommands = new ArrayList<>();
    protected final ArrayList<String> linkerCommands = new ArrayList<>();

    public final ArrayList<String> headerDirs = new ArrayList<>();
    public final ArrayList<String> cppInclude = new ArrayList<>();
    public final ArrayList<String> cppExclude = new ArrayList<>();

    public HashMap<String, String> environment = new HashMap<>();
    public ArrayList<String> cppCompiler = new ArrayList<>();
    public ArrayList<String> linkerCompiler = new ArrayList<>();
    public String compilerOutputCommand = "-o";
    public String linkerOutputCommand = "-o";
    public final ArrayList<String> cppFlags = new ArrayList<>();
    public final ArrayList<String> linkerFlags = new ArrayList<>();
    public String libSuffix = "";
    public String libPrefix = "";
    public String libName = "";
    public String libDirSuffix = "";
    public String linkObjSuffix = "**.o";

    public boolean shouldCompile = true;
    public boolean shouldLink = true;
    public boolean isStatic = false;

    protected CustomFileDescriptor idlDir;
    protected CustomFileDescriptor idlHelperHClasspath;
    protected CustomFileDescriptor idlHelperCPPClasspath;
    protected CustomFileDescriptor idlHelperHFile;
    protected CustomFileDescriptor idlHelperCPPFile;

    protected DefaultBuildTarget() {
    }

    @Override
    protected boolean buildInternal(BuildConfig config) {
        CustomFileDescriptor childTarget = config.buildDir.child(tempBuildDir);
        if(childTarget.exists()) {
            childTarget.deleteDirectory();
        }
        childTarget.mkdirs();

        idlDir = config.buildSourceDir.child("idl");
        if(!idlDir.exists()) {
            idlDir.mkdirs();
        }

        idlHelperHClasspath = new CustomFileDescriptor(helperNameH, CustomFileDescriptor.FileType.Classpath);
        idlHelperCPPClasspath = new CustomFileDescriptor(helperNameCpp, CustomFileDescriptor.FileType.Classpath);
        idlHelperHFile = idlDir.child(idlHelperHClasspath.name());
        idlHelperCPPFile = idlDir.child(idlHelperCPPClasspath.name());

        // Always add IDLHelper, even if not used.
        copyIDLHelperToBuildDir();
        headerDirs.add("-I" + idlDir.path());

        setup(config);
        return build(config, childTarget);
    }

    protected void setup(BuildConfig config) {}

    protected boolean build(BuildConfig config, CustomFileDescriptor buildTargetTemp) {
        ArrayList<CustomFileDescriptor> cppFiles = addSources(config);

        if(shouldCompile && shouldLink && compile(config, buildTargetTemp, cppFiles)) {
            return link(config, buildTargetTemp);
        }
        else if(shouldCompile && !shouldLink) {
            return compile(config, buildTargetTemp, cppFiles);
        }
        else if(!shouldCompile && shouldLink) {
            return link(config, buildTargetTemp);
        }
        else {
            return false;
        }
    }

    protected ArrayList<CustomFileDescriptor> addSources(BuildConfig config) {
        ArrayList<CustomFileDescriptor> cppFiles = getCPPFiles(config.buildSourceDir, cppInclude, cppExclude);
        for(CustomFileDescriptor sourceDir : config.additionalSourceDirs) {
            ArrayList<CustomFileDescriptor> cppFiles1 = getCPPFiles(sourceDir, cppInclude, cppExclude);
            cppFiles.addAll(cppFiles1);
        }
        for(String dir : additionalSourceDirs) {
            CustomFileDescriptor sourceDir = new CustomFileDescriptor(dir);
            ArrayList<CustomFileDescriptor> cppFiles1 = getCPPFiles(sourceDir, cppInclude, cppExclude);
            cppFiles.addAll(cppFiles1);
        }
        return cppFiles;
    }

    protected boolean compile(BuildConfig config, CustomFileDescriptor buildTargetTemp, ArrayList<CustomFileDescriptor> cppFiles) {
        boolean retFlag = false;

        String compiledPaths = "";
        for(CustomFileDescriptor file : cppFiles) {
            String path = file.path();
            compiledPaths = compiledPaths + "\n" + path;
        }
        CustomFileDescriptor cppList = buildTargetTemp.child("cpp.txt");
        cppList.writeString(compiledPaths.trim(), false);

        if(multiCoreCompile) {
            System.out.println("##### COMPILE #####");

            int threads = Runtime.getRuntime().availableProcessors();
            ExecutorService executorService = Executors.newFixedThreadPool(threads);
            ArrayList<Future<?>> futures = new ArrayList<>();

            for(CustomFileDescriptor file : cppFiles) {
                String path = file.path();

                Future<?> submit = executorService.submit(() -> {
                    if(multiCoreCompile) {
                        ArrayList<String> threadCommands = new ArrayList<>();
                        threadCommands.addAll(cppCompiler);
                        threadCommands.addAll(cppFlags);
                        threadCommands.addAll(headerDirs);
                        threadCommands.add(path);
                        boolean flag = JProcess.startProcess(config.buildDir.file(), threadCommands, environment);
                        if(!flag) {
                            multiCoreCompile = false;
                            throw new RuntimeException("Compile Error");
                        }
                    }
                });
                futures.add(submit);
            }

            for (Future<?> future : futures) {
                if(multiCoreCompile) {
                    try {
                        future.get();
                    } catch (Exception e) {
                        e.printStackTrace();
                        multiCoreCompile = false;
                        break;
                    }
                }
                else {
                    break;
                }
            }
            executorService.shutdown();
            if(!multiCoreCompile) {
                return false;
            }
        }
        else {
            compilerCommands.clear();
            compilerCommands.addAll(cppCompiler);
            compilerCommands.addAll(cppFlags);
            compilerCommands.addAll(headerDirs);
            compilerCommands.add("@" + cppList.path());
            System.out.println("##### COMPILE #####");
            boolean flag = JProcess.startProcess(config.buildDir.file(), compilerCommands, environment);
            if(!flag) {
                return false;
            }
        }
        retFlag = true;

        // Manually Move object files to target
        ArrayList<CustomFileDescriptor> files = new ArrayList<>();
        getObjectFiles(config.buildDir, files);
        for(CustomFileDescriptor file : files) {
            file.moveTo(buildTargetTemp);
        }
        return retFlag;
    }

    private boolean link(BuildConfig config, CustomFileDescriptor childTarget) {
        CustomFileDescriptor libDir = config.libDir.child(libDirSuffix);
        if(!libDir.exists()) {
            libDir.mkdirs();
        }
        String libName = this.libName;

        if(libName.isEmpty()) {
            libName = config.libName;
        }

        String libsDir = libDir.path();
        String fullLibName = libPrefix + libName + libSuffix;
        String libPath = libsDir + "/" + fullLibName;

        ArrayList<CustomFileDescriptor> compiledObjects = new ArrayList<>();
        ArrayList<CustomFileDescriptor> allFilesObjects = new ArrayList<>();
        getAllFiles(childTarget, allFilesObjects);
        for(int i = 0; i < allFilesObjects.size(); i++) {
            CustomFileDescriptor customFileDescriptor = allFilesObjects.get(i);
            Path path = customFileDescriptor.file().toPath();
            PathMatcher pathMatcher = FileSystems.getDefault().getPathMatcher("glob:" + linkObjSuffix);
            boolean matches = pathMatcher.matches(path);
            if(matches) {
                compiledObjects.add(customFileDescriptor);
            }
        }

        String compiledPaths = "";
        for(CustomFileDescriptor file : compiledObjects) {
            String path = file.path();
            compiledPaths = compiledPaths + "\n" + path;
        }
        CustomFileDescriptor objList = childTarget.child("objs.txt");
        objList.writeString(compiledPaths.trim(), false);

        linkerCommands.clear();
        onLink(compiledObjects, objList.path(), libPath);

        System.out.println("##### LINK #####");
        return JProcess.startProcess(childTarget.file(), linkerCommands, environment);
    }

    protected void onLink(ArrayList<CustomFileDescriptor> compiledObjects, String objFilePath, String libPath) {
        linkerCommands.addAll(linkerCompiler);
        linkerCommands.add("@" + objFilePath);
        linkerCommands.addAll(linkerFlags);
        linkerCommands.add(linkerOutputCommand + libPath);
    }

    public void addJNIHeaders() {
        headerDirs.add("-Ijni-headers/");
        if(isUnix()) {
            headerDirs.add("-Ijni-headers/linux");
        }
        else if(isWindows()) {
            headerDirs.add("-Ijni-headers/win32");
        }
        else if(isMac()) {
            headerDirs.add("-Ijni-headers/mac");
        }
    }

    public static ArrayList<CustomFileDescriptor> getCPPFiles(CustomFileDescriptor dir, ArrayList<String> cppIncludes, ArrayList<String> cppExcludes) {
        ArrayList<CustomFileDescriptor> files = new ArrayList<>();
        getAllFiles(dir, files);
        for(int i = 0; i < files.size(); i++) {
            // Remove file that does not match
            CustomFileDescriptor fileDescriptor = files.get(i);
            String path = fileDescriptor.path();
            Path of = Path.of(path);
            boolean remove = true;
            for(String cppInclude : cppIncludes) {
                cppInclude = cppInclude.replace("//", "/");
                PathMatcher pathMatcher = FileSystems.getDefault().getPathMatcher("glob:" + cppInclude);
                boolean matches = pathMatcher.matches(of);
                if(matches) {
                    remove = false;
                    break;
                }
            }
            for(String cppExclude : cppExcludes) {
                cppExclude = cppExclude.replace("//", "/");
                PathMatcher pathMatcher = FileSystems.getDefault().getPathMatcher("glob:" + cppExclude);
                boolean matches = pathMatcher.matches(of);
                if(matches) {
                    remove = true;
                    break;
                }
            }
            if(remove) {
                files.remove(i);
                i--;
            }
        }
        return files;
    }

    public static void getAllFiles(CustomFileDescriptor file, ArrayList<CustomFileDescriptor> out) {
        CustomFileDescriptor[] list = file.list();
        for(CustomFileDescriptor fileDescriptor : list) {
            if(fileDescriptor.isDirectory()) {
                getAllFiles(fileDescriptor, out);
            }
            else {
                out.add(fileDescriptor);
            }
        }
    }

    public static void getObjectFiles(CustomFileDescriptor file, ArrayList<CustomFileDescriptor> out) {
        CustomFileDescriptor[] list = file.list();
        for(CustomFileDescriptor fileDescriptor : list) {
            if(!fileDescriptor.isDirectory()) {
                String path = fileDescriptor.path();
                if(path.endsWith(".o") || path.endsWith(".obj")) {
                    out.add(fileDescriptor);
                }
            }
        }
    }

    public void copyIDLHelperToBuildDir() {
        idlHelperHClasspath.copyTo(idlDir, false);
        idlHelperCPPClasspath.copyTo(idlDir, false);
    }
}