package com.github.xpenatan.jparser.builder.targets;

import com.github.xpenatan.jparser.builder.BuildTarget;

public class WindowsTarget extends BuildTarget {
    public WindowsTarget() {
        this.tempBuildDir = "target/windows";

        cFlags = "-c -Wall -O2 -mfpmath=sse -msse2 -fmessage-length=0 -m64";
        cppFlags = "-c -Wall -O2 -mfpmath=sse -msse2 -fmessage-length=0 -m64 -std=c++11";
        linkerFlags = "-Wl,--kill-at -shared -static -static-libgcc -static-libstdc++ -m64";
        libSuffix = "64.dll";

        addJNIHeadersAndGlueCode();
    }
}