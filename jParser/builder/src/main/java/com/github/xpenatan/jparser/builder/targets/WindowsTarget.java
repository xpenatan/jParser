package com.github.xpenatan.jparser.builder.targets;

import com.github.xpenatan.jparser.builder.BuildConfig;
import com.github.xpenatan.jparser.builder.BuildTarget;

public class WindowsTarget extends BuildTarget {

    public boolean addJNI = true;

    public WindowsTarget() {
        this.libDirSuffix = "windows/";
        this.tempBuildDir = "target/windows";

        cppFlags.add("-c");
        cppFlags.add("-Wall");
        cppFlags.add("-O2");
//        cppFlags.add("-mfpmath=sse");
//        cppFlags.add("-msse2");
//        cppFlags.add("-fmessage-length=0");
//        cppFlags.add("-m64");
////        cppFlags.add("-std=c++17");
//        cppFlags.add("-Wno-unused-variable");
//        cppFlags.add("-Wno-unused-but-set-variable");
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
//            linkerFlags.add("-fPIC");
            linkerFlags.add("-shared");
//            linkerFlags.add("-static");
//            linkerFlags.add("-static-libgcc");
//            linkerFlags.add("-static-libstdc++");
//            linkerFlags.add("-Wl,--kill-at");
//            linkerFlags.add("-m64");
            libSuffix = "64.dll";
        }

        if(addJNI) {
            addJNIHeadersAndGlueCode();
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
