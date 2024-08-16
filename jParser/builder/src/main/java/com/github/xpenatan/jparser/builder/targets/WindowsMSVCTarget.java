package com.github.xpenatan.jparser.builder.targets;

import com.github.xpenatan.jparser.builder.BuildConfig;
import com.github.xpenatan.jparser.builder.DefaultBuildTarget;

public class WindowsMSVCTarget extends DefaultBuildTarget {

    public static boolean DEBUG_BUILD;

    // https://learn.microsoft.com/en-us/cpp/build/reference/compiler-options-listed-by-category?view=msvc-170

    public WindowsMSVCTarget() {
        this.libDirSuffix = "windows/vc/";
        this.tempBuildDir = "target/windows";
        linkObjSuffix = ".obj";

        cppCompiler.clear();
        linkerCompiler.clear();

        cppCompiler.add("cmd");
        cppCompiler.add("/c");
        cppCompiler.add("vcvars64.bat");
        cppCompiler.add("&");
        cppCompiler.add("cl");
        compilerOutputCommand = "-Fo:";
        cppFlags.add("-std:c++17");
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
        linkerCompiler.add("vcvars64.bat");
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
}
