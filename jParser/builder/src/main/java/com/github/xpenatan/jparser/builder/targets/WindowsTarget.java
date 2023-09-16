package com.github.xpenatan.jparser.builder.targets;

import com.github.xpenatan.jparser.builder.BuildTarget;

public class WindowsTarget extends BuildTarget {

    public boolean addJNI = true;

    public WindowsTarget() {
        this.tempBuildDir = "target/windows";

        cFlags.add("-c -Wall -O2 -mfpmath=sse -msse2 -fmessage-length=0 -m64");
        cppFlags.add("-c -Wall -O2 -mfpmath=sse -msse2 -fmessage-length=0 -m64 -std=c++11");
        cppFlags.add("-Wno-unused-variable");
        cppFlags.add("-Wno-unused-but-set-variable");
        linkerFlags.add("-Wl,--kill-at -shared -static -static-libgcc -static-libstdc++ -m64");
        libSuffix = "64.dll";

        if(addJNI) {
            addJNIHeadersAndGlueCode();
        }
    }
}
