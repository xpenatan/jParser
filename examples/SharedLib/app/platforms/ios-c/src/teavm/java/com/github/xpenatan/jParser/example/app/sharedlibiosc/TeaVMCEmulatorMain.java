package com.github.xpenatan.jParser.example.app.sharedlibiosc;

import com.github.xpenatan.jParser.example.app.SharedLib;
import com.github.xpenatan.jParser.loader.JParserLibraryLoaderListener;
import com.github.xpenatan.jparser.runtime.RuntimeLoader;
import libA.LibALoader;
import libB.LibBLoader;

/** Java entry point embedded in the handwritten SharedLib iOS emulator app. */
public class TeaVMCEmulatorMain {

    public static void main(String[] args) {
        RuntimeLoader.init(requiredLoader("runtime"));
        LibALoader.init(requiredLoader("LibA"));
        LibBLoader.init(requiredLoader("LibB"));

        if(!SharedLib.test()) {
            throw new AssertionError("SharedLib TeaVM C test failed");
        }
        System.out.println("SharedLib iOS TeaVM C binding test passed");
    }

    private static JParserLibraryLoaderListener requiredLoader(String libraryName) {
        return (success, error) -> {
            if(!success) {
                throw new IllegalStateException("Unable to load " + libraryName, error);
            }
        };
    }
}
