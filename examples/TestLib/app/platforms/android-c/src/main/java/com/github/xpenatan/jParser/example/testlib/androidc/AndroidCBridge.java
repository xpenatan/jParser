package com.github.xpenatan.jParser.example.testlib.androidc;

public class AndroidCBridge {

    static {
        System.loadLibrary("runtime");
        System.loadLibrary("TestLib");
        System.loadLibrary("TestLibTeaVMCApp");
    }

    public static native boolean runTestLibTest();
}
