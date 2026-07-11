package com.github.xpenatan.jParser.example.app.sharedlibc;

import com.github.xpenatan.jParser.example.app.SharedLib;
import com.github.xpenatan.jParser.loader.JParserLibraryLoaderListener;
import com.github.xpenatan.jparser.runtime.RuntimeLoader;
import libA.LibALoader;
import libB.LibBLoader;

public class TeaVMCHeadlessMain {

    public static void main(String[] args) {
        RuntimeLoader.init(requiredLoader("runtime"));
        LibALoader.init(requiredLoader("LibA"));
        LibBLoader.init(requiredLoader("LibB"));

        if(!SharedLib.test()) {
            throw new AssertionError("SharedLib TeaVM C test failed");
        }
        System.out.println("SharedLib TeaVM C test passed");
    }

    private static JParserLibraryLoaderListener requiredLoader(String libraryName) {
        return (success, error) -> {
            if(!success) {
                throw new IllegalStateException("Unable to load " + libraryName, error);
            }
        };
    }
}
