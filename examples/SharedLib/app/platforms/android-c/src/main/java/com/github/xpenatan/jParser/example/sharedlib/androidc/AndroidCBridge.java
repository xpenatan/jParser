package com.github.xpenatan.jParser.example.sharedlib.androidc;

public class AndroidCBridge {

    static {
        System.loadLibrary("runtime");
        System.loadLibrary("LibA");
        System.loadLibrary("LibB");
        System.loadLibrary("SharedLibTeaVMCApp");
    }

    public static native boolean runSharedLibTest();
}
