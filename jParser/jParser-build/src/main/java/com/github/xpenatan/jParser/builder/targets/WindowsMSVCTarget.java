package com.github.xpenatan.jParser.builder.targets;

import com.github.xpenatan.jParser.builder.BuildConfig;
import com.github.xpenatan.jParser.builder.DefaultBuildTarget;
import com.github.xpenatan.jParser.core.util.CustomFileDescriptor;
import java.util.ArrayList;

public class WindowsMSVCTarget extends DefaultBuildTarget {

    public static boolean DEBUG_BUILD;

    // https://learn.microsoft.com/en-us/cpp/build/reference/compiler-options-listed-by-category?view=msvc-170

    public WindowsMSVCTarget() {
        this.libDirSuffix = "windows/vc/";
        this.tempBuildDir = "target/windows/";
        linkObjSuffix = "**.obj";

        cppCompiler.clear();
        linkerCompiler.clear();

        cppCompiler.add("cmd");
        cppCompiler.add("/c");
        cppCompiler.add("vcvarsall");
        cppCompiler.add("x64");
        cppCompiler.add("&");
        cppCompiler.add("cl");
        compilerOutputCommand = "-Fo:";
        cppFlags.add("-c");
        if(DEBUG_BUILD) {
            cppFlags.add("/Z7"); // add debug information in .obj to work in visual studio
            cppFlags.add("/Od");
        }
        else {
            cppFlags.add("/O2");
        }
        linkerOutputCommand = "/OUT:";
        libSuffix = "64.dll";
    }

    @Override
    protected void setup(BuildConfig config) {
        linkerCompiler.add("cmd");
        linkerCompiler.add("/c");
        linkerCompiler.add("vcvarsall");
        linkerCompiler.add("x64");
        linkerCompiler.add("&");
        if(isStatic) {
            linkerCompiler.add("lib");
            libSuffix = "64_.lib";
        }
        else {
            linkerCompiler.add("link");
            if(DEBUG_BUILD) {
                linkerCompiler.add("/DEBUG"); // Generates .pbd file
            }
            linkerFlags.add("-DLL");
        }
        linkerCompiler.add("/NOLOGO");
        linkerCompiler.add("/MACHINE:X64");
    }

    @Override
    protected boolean compile(BuildConfig config, CustomFileDescriptor buildTargetTemp, ArrayList<CustomFileDescriptor> cppFiles) {
        boolean multiCoreCompile = this.multiCoreCompile;
        this.multiCoreCompile = false;
        if(multiCoreCompile) {
            // Use native MSVC multi core support
            cppCompiler.add("/MP");
        }
        boolean compile = super.compile(config, buildTargetTemp, cppFiles);
        this.multiCoreCompile = multiCoreCompile;
        return compile;
    }
}
