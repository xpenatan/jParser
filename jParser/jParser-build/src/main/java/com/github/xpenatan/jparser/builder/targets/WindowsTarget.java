package com.github.xpenatan.jparser.builder.targets;

import com.github.xpenatan.jparser.builder.BuildConfig;
import com.github.xpenatan.jparser.builder.DefaultBuildTarget;
import com.github.xpenatan.jparser.core.util.CustomFileDescriptor;
import java.util.ArrayList;

public class WindowsTarget extends DefaultBuildTarget {

    public static boolean DEBUG_BUILD;

    public WindowsTarget() {
        this.libDirSuffix = "windows/";
        this.tempBuildDir = "target/windows/";

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
        cppFlags.add("-std=c++17");
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
        }
        else {
//            linkerFlags.add("-fPIC");
            linkerFlags.add("-shared");
            linkerFlags.add("-static");
            linkerFlags.add("-static-libgcc");
            linkerFlags.add("-static-libstdc++");
//            linkerFlags.add("-Wl,--kill-at");
            linkerFlags.add("-m64");
            libSuffix = "64.dll";
        }

    }

    @Override
    protected void onLink(ArrayList<CustomFileDescriptor> compiledObject, String objFilePath, String libPath) {
        if(isStatic) {
            linkerCommands.addAll(linkerCompiler);
            linkerCommands.addAll(linkerFlags);
            linkerCommands.add(libPath);
            linkerCommands.add("@" + objFilePath);
        }
        else {
            super.onLink(compiledObject, objFilePath, libPath);
        }
    }
}
