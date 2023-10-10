package com.github.xpenatan.jparser.builder.targets;

import com.github.xpenatan.jparser.builder.BuildTarget;

public class IOSTarget extends BuildTarget {
    public IOSTarget() {
        this.tempBuildDir = "target/ios";

        cppFlags.add("-c");
        cppFlags.add("-Wall");
        cppFlags.add("-O2");
        cppFlags.add("-stdlib=libc++");
        linkerFlags.add("-shared");
        linkerFlags.add("-stdlib=libc++");

        cppIncludes.add("**/jniglue/JNIGlue.cpp");
        headerDirs.add("jni-headers/");
        headerDirs.add("jni-headers/mac");

        cppCompiler.clear();
        linkerCompiler.clear();
        cppCompiler.add("clang++");
        libSuffix = "64.dylib";
    }
}