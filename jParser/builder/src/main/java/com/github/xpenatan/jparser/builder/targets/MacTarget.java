package com.github.xpenatan.jparser.builder.targets;

import com.github.xpenatan.jparser.builder.BuildConfig;
import com.github.xpenatan.jparser.builder.DefaultBuildTarget;
import com.github.xpenatan.jparser.core.util.CustomFileDescriptor;
import java.util.ArrayList;

public class MacTarget extends DefaultBuildTarget {

    private boolean isArm = false;

    public MacTarget() {
        this(false);
    }

    public MacTarget(boolean isArm) {
        this.isArm = isArm;

        if(isArm) {
            this.libDirSuffix = "mac/arm/";
            this.tempBuildDir = "target/mac/arm";
        }
        else {
            this.libDirSuffix = "mac/";
            this.tempBuildDir = "target/mac";
        }
        this.libPrefix = "lib";

        cppCompiler.clear();
        linkerCompiler.clear();
        String cppCompilerr = "clang++";
        cppCompiler.add(cppCompilerr);
        linkerCompiler.add(cppCompilerr);

        cppFlags.add("-march=x86-6");
        cppFlags.add("-c");
        cppFlags.add("-Wall");
        cppFlags.add("-O2");
        if(isArm) {
            cppFlags.add("-arch arm64");
        }
        else {
            cppFlags.add("-arch x86_64");
        }
        cppFlags.add("-DFIXED_POINT");

        cppFlags.add("-mfpmath=sse");
        cppFlags.add("-msse2");
        cppFlags.add("-fmessage-length=0");
        cppFlags.add("-fPIC");
        cppFlags.add("-std=c++17");
        cppFlags.add("-Wno-unused-variable");
        cppFlags.add("-Wno-unused-but-set-variable");
        cppFlags.add("-w");
        cppFlags.add("-Wno-format");

        cppFlags.add("-mmacosx-version-min=10.7");
        cppFlags.add("-stdlib=libc++");
    }

    @Override
    protected void setup(BuildConfig config) {
        if(isStatic) {
            linkerCompiler.clear();
            linkerCompiler.add("libtool");
            linkerFlags.add("-static");
            linkerFlags.add("-o");
            libSuffix = "64.a";
        }
        else {
            linkerFlags.add("-shared");
            if(isArm) {
                cppFlags.add("-arch arm64");
                libSuffix = "arm64.dylib";
            }
            else {
                cppFlags.add("-arch x86_64");
                libSuffix = "64.dylib";
            }
            linkerFlags.add("-mmacosx-version-min=10.7");
            linkerFlags.add("-stdlib=libc++");
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