package com.github.xpenatan.jparser.builder.targets;

import com.github.xpenatan.jparser.builder.BuildConfig;
import com.github.xpenatan.jparser.builder.BuildTarget;

public class IOSTarget extends BuildTarget {
    public IOSTarget() {
        this.libDirSuffix = "ios/";
        this.tempBuildDir = "target/ios";

        cppCompiler.clear();
        linkerCompiler.clear();
        cppCompiler.add("clang++");
        linkerCompiler.add("clang++");
        libSuffix = "dylib";

        cppFlags.add("-c");
        cppFlags.add("-Wall");
        cppFlags.add("-O2");
        cppFlags.add("-stdlib=libc++");
        linkerFlags.add("-shared");
        linkerFlags.add("-stdlib=libc++");

        cppInclude.add("**/jniglue/JNIGlue.cpp");
        headerDirs.add("jni-headers/");
        headerDirs.add("jni-headers/mac");
    }

    @Override
    protected void setup(BuildConfig config) {
        if(isStatic) {
            linkerCompiler.clear();
            linkerCompiler.add("ar");
            linkerFlags.add("rcs");
            libSuffix = "64.a";
        }
        else {
            linkerFlags.add("-shared");
            linkerFlags.add("-stdlib=libc++");
            libSuffix = "64.so";
        }
    }

    @Override
    protected void onLink(String objFilePath, String libPath) {
        if(isStatic) {
            linkerCommands.addAll(linkerCompiler);
            linkerCommands.addAll(linkerFlags);
            linkerCommands.add(libPath);
            linkerCommands.add("@" + objFilePath);
        }
        else {
            super.onLink(objFilePath, libPath);
        }
    }
}