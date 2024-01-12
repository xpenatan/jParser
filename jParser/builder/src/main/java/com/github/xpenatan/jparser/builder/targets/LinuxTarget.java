package com.github.xpenatan.jparser.builder.targets;

import com.github.xpenatan.jparser.builder.BuildConfig;
import com.github.xpenatan.jparser.builder.BuildTarget;

public class LinuxTarget extends BuildTarget {

    public LinuxTarget() {
        this.libDirSuffix = "linux/";
        this.tempBuildDir = "target/linux";
        this.libPrefix = "lib";

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
        cppFlags.add("-w");
        cppFlags.add("-Wno-format");
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
            linkerFlags.add("-m64");
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