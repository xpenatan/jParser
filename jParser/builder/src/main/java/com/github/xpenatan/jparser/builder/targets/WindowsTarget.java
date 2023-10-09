package com.github.xpenatan.jparser.builder.targets;

import com.github.xpenatan.jparser.builder.BuildTarget;

public class WindowsTarget extends BuildTarget {

    public boolean addJNI = true;

    public WindowsTarget() {
        this.tempBuildDir = "target/windows";

        cppFlags.add("-c");
        cppFlags.add("-Wall");
        cppFlags.add("-O2");
        cppFlags.add("-mfpmath=sse");
        cppFlags.add("-msse2");
        cppFlags.add("-fmessage-length=0");
        cppFlags.add("-m64");
        cppFlags.add("-std=c++17");
        cppFlags.add("-Wno-unused-variable");
        cppFlags.add("-Wno-unused-but-set-variable");
        linkerFlags.add("-Wl,--kill-at");
        linkerFlags.add("-shared");
        linkerFlags.add("-static");
        linkerFlags.add("-static-libgcc");
        linkerFlags.add("-static-libstdc++");
        linkerFlags.add("-m64");
        libSuffix = "64.dll";

        if(addJNI) {
            addJNIHeadersAndGlueCode();
        }
    }
}
