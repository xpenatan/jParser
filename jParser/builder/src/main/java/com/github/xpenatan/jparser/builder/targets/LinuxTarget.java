package com.github.xpenatan.jparser.builder.targets;

import com.github.xpenatan.jparser.builder.BuildTarget;

public class LinuxTarget extends BuildTarget {

    public LinuxTarget() {
        this.libDirSuffix = "linux/";
        this.tempBuildDir = "target/linux";

        cppFlags.add("-c");
        cppFlags.add("-Wall");
        cppFlags.add("-O2");
        cppFlags.add("-mfpmath=sse");
        cppFlags.add("-msse2");
        cppFlags.add("-fmessage-length=0");
        cppFlags.add("-m64");
        cppFlags.add("-fPIC");
        cppFlags.add("-std=c++17");
        cppFlags.add("-Wno-unused-variable");
        cppFlags.add("-Wno-unused-but-set-variable");
        linkerFlags.add("-Wl,-wrap,memcpy");
        linkerFlags.add("-shared");
        linkerFlags.add("-m64");
        libSuffix = "64.so";
        libPrefix = "lin";
    }
}
