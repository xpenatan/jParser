package com.github.xpenatan.jparser.builder;

import com.github.xpenatan.jparser.core.util.CustomFileDescriptor;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.ArrayList;

public abstract class DefaultBuildTarget extends BuildTarget {

    public String tempBuildDir;

    private final ArrayList<String> compilerCommands = new ArrayList<>();
    protected final ArrayList<String> linkerCommands = new ArrayList<>();

    public final ArrayList<String> headerDirs = new ArrayList<>();
    public final ArrayList<String> cppInclude = new ArrayList<>();
    public final ArrayList<String> cppExclude = new ArrayList<>();

    /** Includes only files with this suffix */
    public String filterCPPSuffix = ".cpp";
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
    public String linkObjSuffix = ".o";

    public boolean shouldCompile = true;
    public boolean shouldLink = true;

    public boolean isStatic = false;

    protected DefaultBuildTarget() {
        cppCompiler.add("x86_64-w64-mingw32-g++");
        linkerCompiler.add("x86_64-w64-mingw32-g++");
    }

    protected void setup(BuildConfig config) {}

    protected boolean build(BuildConfig config) {
        CustomFileDescriptor childTarget = config.buildDir.child(tempBuildDir);
        if(childTarget.exists()) {
            childTarget.deleteDirectory();
        }
        childTarget.mkdirs();

        setup(config);

        ArrayList<CustomFileDescriptor> cppFiles = new ArrayList<>(getCPPFiles(config.sourceDir, cppInclude, cppExclude, filterCPPSuffix));
        for(CustomFileDescriptor sourceDir : config.additionalSourceDirs) {
            ArrayList<CustomFileDescriptor> cppFiles1 = getCPPFiles(sourceDir, cppInclude, cppExclude, filterCPPSuffix);
            cppFiles.addAll(cppFiles1);
        }

        if(shouldCompile && shouldLink && compile(config, childTarget, cppFiles)) {
            return link(config, childTarget);
        }
        else if(shouldCompile && !shouldLink) {
            return compile(config, childTarget, cppFiles);
        }
        else if(!shouldCompile && shouldLink) {
            return link(config, childTarget);
        }
        else {
            return false;
        }
    }

    private boolean compile(BuildConfig config, CustomFileDescriptor childTarget, ArrayList<CustomFileDescriptor> cppFiles) {
        boolean retFlag = false;

        String compiledPaths = "";
        for(CustomFileDescriptor file : cppFiles) {
            String path = file.path();
            compiledPaths = compiledPaths + "\n" + path;
        }
        CustomFileDescriptor cppList = childTarget.child("cpp.txt");
        cppList.writeString(compiledPaths.trim(), false);

        compilerCommands.clear();
        compilerCommands.addAll(cppCompiler);
        compilerCommands.addAll(cppFlags);
        compilerCommands.addAll(headerDirs);
        compilerCommands.add("@" + cppList.path());
        System.err.println("##### COMPILE #####");
        boolean flag = JProcess.startProcess(config.buildDir.file(), compilerCommands);
        if(!flag) {
            return false;
        }
        retFlag = true;

        // Manually Move object files to target
        ArrayList<CustomFileDescriptor> files = new ArrayList<>();
        getObjectFiles(config.buildDir, files);
        for(CustomFileDescriptor file : files) {
            file.moveTo(childTarget);
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
        getAllFiles(childTarget, compiledObjects, linkObjSuffix);

        String compiledPaths = "";
        for(CustomFileDescriptor file : compiledObjects) {
            String path = file.path();
            compiledPaths = compiledPaths + "\n" + path;
        }
        CustomFileDescriptor objList = childTarget.child("objs.txt");
        objList.writeString(compiledPaths.trim(), false);

        linkerCommands.clear();
        onLink(compiledObjects, objList.path(), libPath);

        System.err.println("##### LINK #####");
        return JProcess.startProcess(childTarget.file(), linkerCommands);
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

    public static ArrayList<CustomFileDescriptor> getCPPFiles(CustomFileDescriptor dir, ArrayList<String> cppIncludes, ArrayList<String> cppExcludes, String cppSuffix) {
        ArrayList<CustomFileDescriptor> files = new ArrayList<>();
        getAllFiles(dir, files, cppSuffix);
        for(int i = 0; i < files.size(); i++) {
            // Remove file that does not match
            CustomFileDescriptor fileDescriptor = files.get(i);
            String path = fileDescriptor.path();
            Path of = Path.of(path);
            boolean remove = true;
            for(String cppInclude : cppIncludes) {
                PathMatcher pathMatcher = FileSystems.getDefault().getPathMatcher("glob:" + cppInclude);
                boolean matches = pathMatcher.matches(of);
                if(matches) {
                    remove = false;
                    break;
                }
            }
            for(String cppExclude : cppExcludes) {
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

    public static void getAllFiles(CustomFileDescriptor file, ArrayList<CustomFileDescriptor> out, String suffix) {
        CustomFileDescriptor[] list = file.list();
        for(CustomFileDescriptor fileDescriptor : list) {
            if(fileDescriptor.isDirectory()) {
                getAllFiles(fileDescriptor, out, suffix);
            }
            else {
                if(!suffix.isEmpty()) {
                    if(fileDescriptor.path().endsWith(suffix)) {
                        out.add(fileDescriptor);
                    }
                }
                else {
                    out.add(fileDescriptor);
                }
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
}