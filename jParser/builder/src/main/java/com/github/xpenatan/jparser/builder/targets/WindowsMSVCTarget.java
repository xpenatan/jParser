package com.github.xpenatan.jparser.builder.targets;

import com.github.xpenatan.jparser.builder.BuildTarget;

public class WindowsMSVCTarget extends BuildTarget {

    public boolean addJNI = true;

    public WindowsMSVCTarget() {
        this.tempBuildDir = "target/windows";

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
        linkerCompiler.add("vcvars64.bat");
        linkerCompiler.add("&");
        linkerCompiler.add("link");
        linkerCompiler.add("/DLL");
        linkerCompiler.add("/NOLOGO");
        linkerCompiler.add("/MACHINE:X64");
        libSuffix = "64.dll";

        if(addJNI) {
            addJNIHeadersAndGlueCode();
        }
    }
}
