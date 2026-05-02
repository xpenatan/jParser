package com.github.xpenatan.jParser.builder.targets;

import com.github.xpenatan.jParser.builder.BuildConfig;
import com.github.xpenatan.jParser.builder.DefaultBuildTarget;

public class WindowsMingwTarget extends DefaultBuildTarget {

    public static boolean DEBUG_BUILD;

    public WindowsMingwTarget() {
        this(SourceLanguage.CPP);
    }

    public WindowsMingwTarget(SourceLanguage language) {
        this.libDirSuffix = "windows/";
        this.tempBuildDir = "target/windows/";

        if(language == SourceLanguage.C) {
            cppCompiler.add("x86_64-w64-mingw32-gcc");
            linkerCompiler.add("x86_64-w64-mingw32-gcc");
        }
        else if(language == SourceLanguage.CPP) {
            cppCompiler.add("x86_64-w64-mingw32-g++");
            linkerCompiler.add("x86_64-w64-mingw32-g++");
        }

        cppFlags.add("-c");
        cppFlags.add("-Wall");
        if(DEBUG_BUILD) {
            cppFlags.add("-O0");
            cppFlags.add("-g");
        }
        else {
            cppFlags.add("-O2");
        }
        cppFlags.add("-fmessage-length=0");
        cppFlags.add("-m64");
        cppFlags.add("-w");
        cppFlags.add("-Wno-format");

    }

    @Override
    protected void setup(BuildConfig config) {
        if(isStatic) {
            linkerCompiler.clear();
            linkerCompiler.add("ar");
            linkerFlags.add("rcs");
            libSuffix = "64_.a";
            linkerOutputCommand = "";
        }
        else {
            linkerFlags.add("-shared");
            linkerFlags.add("-static");
            linkerFlags.add("-static-libgcc");
            linkerFlags.add("-static-libstdc++");
            linkerFlags.add("-m64");
            libSuffix = "64.dll";
            linkerOutputCommand = "-o";
        }

    }
}
