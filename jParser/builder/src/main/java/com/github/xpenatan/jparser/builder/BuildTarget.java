package com.github.xpenatan.jparser.builder;

import com.github.xpenatan.jparser.core.util.CustomFileDescriptor;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.ArrayList;

public abstract class BuildTarget {

    private static String OS = System.getProperty("os.name").toLowerCase();

    public static boolean isWindows() {
        return OS.contains("win");
    }

    public static boolean isMac() {
        return OS.contains("mac");
    }

    public static boolean isUnix() {
        return (OS.contains("nix") || OS.contains("nux") || OS.contains("aix") || OS.contains("Linux"));
    }

    public String tempBuildDir;

    private final ArrayList<String> compilerCommands = new ArrayList<>();
    private final ArrayList<String> linkerCommands = new ArrayList<>();

    public final ArrayList<String> headerDirs = new ArrayList<>();
    public final ArrayList<String> cppIncludes = new ArrayList<>();
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

    public boolean isCompile = true;
    public boolean isLink = true;

    protected BuildTarget() {
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

        ArrayList<CustomFileDescriptor> cppFiles = getCPPFiles(config.sourceDir, cppIncludes);

        if(isCompile && isLink && compile(config, childTarget, cppFiles)) {
            return link(config, childTarget);
        }
        else if(isCompile && !isLink) {
            return compile(config, childTarget, cppFiles);
        }
        else if(!isCompile && isLink) {
            return link(config, childTarget);
        }
        else {
            return false;
        }
    }

    private boolean compile(BuildConfig config, CustomFileDescriptor childTarget, ArrayList<CustomFileDescriptor> cppFiles) {
        boolean retFlag = false;

        for(CustomFileDescriptor file : cppFiles) {
            String path = file.path();
            String sourceBasePath = config.sourceDir.path();
            String pathWithoutBase = file.path().replace(sourceBasePath, "");
            String pathName = file.name();
            String parent = pathWithoutBase.replace(pathName, "");
            CustomFileDescriptor toCopy = childTarget.child(parent);
            String p = toCopy.path();
            if(!toCopy.exists()) {
                toCopy.mkdirs();
            }
            p += "/";
            String compiledPath = p + file.nameWithoutExtension() + ".o";

            compilerCommands.clear();
            compilerCommands.addAll(cppCompiler);
            compilerCommands.addAll(cppFlags);
            compilerCommands.addAll(headerDirs);
            compilerCommands.add(path);
            compilerCommands.add(compilerOutputCommand + compiledPath);
            System.out.println("##### COMPILE #####");
            boolean flag = JProcess.startProcess(config.buildDir.file(), compilerCommands);
            if(!flag) {
                return false;
            }
            retFlag = true;
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

        ArrayList<CustomFileDescriptor> files = new ArrayList<>();
        getAllFiles(childTarget, files, "");

        String compiledPaths = "";
        for(CustomFileDescriptor file : files) {
            String path = file.path();
            compiledPaths = compiledPaths + "\n" + path;
        }
        CustomFileDescriptor objList = childTarget.child("objs.txt");
        objList.writeString(compiledPaths.trim(), false);

        linkerCommands.clear();
        linkerCommands.addAll(linkerCompiler);
        linkerCommands.add("@" + objList.path());
        linkerCommands.addAll(linkerFlags);
        linkerCommands.add(linkerOutputCommand + libPath);

        System.out.println("##### LINK #####");
        return JProcess.startProcess(childTarget.file(), linkerCommands);
    }

    protected void addJNIHeadersAndGlueCode() {
        cppIncludes.add("**/jniglue/*.cpp");
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

    public static ArrayList<CustomFileDescriptor> getCPPFiles(CustomFileDescriptor dir, ArrayList<String> cppIncludes) {
        ArrayList<CustomFileDescriptor> files = new ArrayList<>();
        getAllFiles(dir, files, ".cpp");
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
}