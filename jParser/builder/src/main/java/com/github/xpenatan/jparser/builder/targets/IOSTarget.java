package com.github.xpenatan.jparser.builder.targets;

import com.github.xpenatan.jparser.builder.BuildTarget;

public class IOSTarget extends BuildTarget {
    public IOSTarget() {
        this.tempBuildDir = "target/ios";

        cFlags.add("-c -Wall -O2 -stdlib=libc++");
        cppFlags.add("-c -Wall -O2 -stdlib=libc++");
        linkerFlags.add("-shared -stdlib=libc++");

        cppIncludes.add("**/jniglue/JNIGlue.cpp");
        headerDirs.add("jni-headers/");
        headerDirs.add("jni-headers/mac");

        cCompiler = "clang";
        cppCompiler = "clang++";
        libSuffix = "64.dylib";
    }
}