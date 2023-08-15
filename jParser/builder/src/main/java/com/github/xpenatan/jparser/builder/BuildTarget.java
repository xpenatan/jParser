package com.github.xpenatan.jparser.builder;

import com.github.xpenatan.jparser.core.util.CustomFileDescriptor;
import java.io.File;
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
    public final ArrayList<String> headerDirs = new ArrayList<>();
    public final ArrayList<String> cIncludes = new ArrayList<>();
    public final ArrayList<String> cppIncludes = new ArrayList<>();
    public String cppCompiler = "x86_64-w64-mingw32-g++";
    public String cCompiler = "x86_64-w64-mingw32-gcc";
    public final ArrayList<String> cppFlags = new ArrayList<>();
    public final ArrayList<String> cFlags = new ArrayList<>();
    public String linkerFlags = "";
    public String libSuffix;

    protected BuildTarget() {
    }

    protected void setup(BuildConfig config, ArrayList<CustomFileDescriptor> cppFiles) {}

    protected boolean build(BuildConfig config) {

        copyJniHeaders(config.buildDir);

        CustomFileDescriptor childTarget = config.buildDir.child(tempBuildDir);
        if(childTarget.exists()) {
            childTarget.delete();
        }
        childTarget.mkdirs();

        ArrayList<CustomFileDescriptor> cppFiles = getCPPFiles(config.sourceDir, cppIncludes);
        setup(config, cppFiles);

        if(compile(config, childTarget, cppFiles)) {
            return link(config, childTarget);
        }
        else {
            return false;
        }
    }

    private boolean compile(BuildConfig config, CustomFileDescriptor childTarget, ArrayList<CustomFileDescriptor> cppFiles) {
        System.out.println("##### COMPILE #####");
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

            String headers = "";
            for(String headerDir : headerDirs) {
                headers += "-I" + headerDir + " ";
            }

            String cppFlag = "";
            for(String flag : cppFlags) {
                cppFlag = cppFlag + " " + flag;
            }
            cppFlag = cppFlag.replaceAll("\\s+", " ").trim();
            String command = cppCompiler;
            command = command + " " + cppFlag;
            command = command + " " + headers;
            command = command + " " + path;
            command = command + " -o " + compiledPath;
            boolean flag = JProcess.startProcess(config.buildDir.file(), command);
            if(!flag) {
                return false;
            }
            retFlag = true;
        }
        return retFlag;
    }

    private boolean link(BuildConfig config, CustomFileDescriptor childTarget) {
        if(!config.libsDir.exists()) {
            config.libsDir.mkdirs();
        }

        String libsDir = config.libsDir.path();
        String libName = config.libName + libSuffix;
        String libPath = libsDir + File.separator + libName;

        ArrayList<CustomFileDescriptor> files = new ArrayList<>();
        getAllFiles(childTarget, files, ".o");

        String compiledPaths = "";

        for(CustomFileDescriptor file : files) {
            String path = file.path();
            compiledPaths = compiledPaths + " " + path;
        }

        String command = cppCompiler;
        command = command + " " + linkerFlags;
        command = command + " -o " + libPath;
        command = command + " " + compiledPaths;
        System.out.println("##### LINK #####");
        return JProcess.startProcess(childTarget.file(), command);
    }

    private void copyJniHeaders (CustomFileDescriptor buildDir) {
        final String pack = "headers";
        String files[] = {"classfile_constants.h", "jawt.h", "jdwpTransport.h", "jni.h", "linux/jawt_md.h", "linux/jni_md.h",
                "mac/jni_md.h", "win32/jawt_md.h", "win32/jni_md.h"};

        for (String file : files) {
            CustomFileDescriptor child = buildDir.child("jni-headers").child(file);
            new CustomFileDescriptor(pack, CustomFileDescriptor.FileType.Classpath).child(file).copyTo(child);
        }
    }

    protected void addJNIHeadersAndGlueCode() {
        cppIncludes.add("**/jniglue/JNIGlue.cpp");
        headerDirs.add("jni-headers/");
        if(isWindows()) {
            headerDirs.add("jni-headers/win32");
        }
        else if(isMac()) {
            headerDirs.add("jni-headers/mac");
        }
        else if(isUnix()) {
            headerDirs.add("jni-headers/linux");
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