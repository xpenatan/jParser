package com.github.xpenatan.jparser.builder.targets;

import com.github.xpenatan.jparser.builder.BuildConfig;
import com.github.xpenatan.jparser.builder.DefaultBuildTarget;

public class WindowsMSVSTarget extends DefaultBuildTarget {

    public WindowsMSVSTarget() {
        this.libDirSuffix = "windows/";
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
        linkerOutputCommand = "/OUT:";
        libSuffix = "64.dll";
    }

    @Override
    protected void setup(BuildConfig config) {
        linkerCompiler.add("vcvars64.bat");
        linkerCompiler.add("&");
        if(isStatic) {
            linkerCompiler.add("lib");
        }
        else {
            linkerCompiler.add("link");
            linkerFlags.add("-DLL");
        }
        linkerCompiler.add("/NOLOGO");
        linkerCompiler.add("/MACHINE:X64");
    }
}
