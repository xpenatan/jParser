package com.github.xpenatan.jParser.builder.targets;

import com.github.xpenatan.jParser.builder.BuildConfig;
import com.github.xpenatan.jParser.builder.DefaultBuildTarget;
import com.github.xpenatan.jParser.core.util.CustomFileDescriptor;
import java.util.ArrayList;

public class LinuxTarget extends DefaultBuildTarget {

    public LinuxTarget() {
        this(SourceLanguage.CPP);
    }

    public LinuxTarget(SourceLanguage language) {
        this.libDirSuffix = "linux/";
        this.tempBuildDir = "target/linux/";
        this.libPrefix = "lib";

        if(language == SourceLanguage.C) {
            String cppCompilerr = "gcc";
            cppCompiler.add(cppCompilerr);
            linkerCompiler.add(cppCompilerr);
        }
        else if(language == SourceLanguage.CPP) {
            String cppCompilerr = "g++";
            cppCompiler.add(cppCompilerr);
            linkerCompiler.add(cppCompilerr);
        }

        cppFlags.add("-c");
        cppFlags.add("-Wall");
        cppFlags.add("-O2");
        cppFlags.add("-fmessage-length=0");
        cppFlags.add("-m64");
        cppFlags.add("-fPIC");
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