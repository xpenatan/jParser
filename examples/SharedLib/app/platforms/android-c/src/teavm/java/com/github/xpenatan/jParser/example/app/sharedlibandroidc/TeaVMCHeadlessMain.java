package com.github.xpenatan.jParser.example.app.sharedlibandroidc;

import com.github.xpenatan.jParser.example.app.SharedLib;
import com.github.xpenatan.jparser.runtime.RuntimeLoader;
import libA.LibALoader;
import libB.LibBLoader;

public class TeaVMCHeadlessMain {

    public static void main(String[] args) {
        RuntimeLoader.init(null);
        LibALoader.init(null);
        LibBLoader.init(null);

        if(!SharedLib.test()) {
            throw new AssertionError("SharedLib Android-C test failed");
        }
        System.out.println("SharedLib Android-C test passed");
    }
}
