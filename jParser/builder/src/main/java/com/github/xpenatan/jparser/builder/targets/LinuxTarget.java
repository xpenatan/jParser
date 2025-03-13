package com.github.xpenatan.jparser.builder.targets;

import com.github.xpenatan.jparser.builder.BuildConfig;
import com.github.xpenatan.jparser.builder.DefaultBuildTarget;
import com.github.xpenatan.jparser.core.util.CustomFileDescriptor;
import java.util.ArrayList;

public class LinuxTarget extends DefaultBuildTarget {

    public LinuxTarget() {
        this.libDirSuffix = "linux/";
        this.tempBuildDir = "target/linux";
        this.libPrefix = "lib";

        cppCompiler.clear();
        linkerCompiler.clear();
        cppCompiler.add("g++");
        linkerCompiler.add("g++");

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
//        -static-libgcc
    }

    @Override
    protected void setup(BuildConfig config) {
        if(isStatic) {
            linkerCompiler.clear();
            linkerCompiler.add("ar");
            linkerFlags.add("rcs");
            libSuffix = "64_.a";
        }
        else {
            // Note:
            // Linux have an issue with libstdc++, libgcc and libc where if the system uses an updated version when compiling, older linux version will fail to run.
            // static linking may fix libstdc++ and libgcc but not libc because it's not possible to static link it.
            linkerFlags.add("-shared");
            linkerFlags.add("-static-libgcc");
            linkerFlags.add("-m64");
            libSuffix = "64.so";
        }
    }

    @Override
    protected void onLink(ArrayList<CustomFileDescriptor> compiledObjects, String objFilePath, String libPath) {
        if(isStatic) {
            linkerCommands.addAll(linkerCompiler);
            linkerCommands.addAll(linkerFlags);
            linkerCommands.add(libPath);
            linkerCommands.add("@" + objFilePath);
        }
        else {
            super.onLink(compiledObjects, objFilePath, libPath);
        }
    }
}