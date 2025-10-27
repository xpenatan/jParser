package com.github.xpenatan.jParser.example.testlib;

import com.github.xpenatan.jParser.loader.JParserLibraryLoader;
import com.github.xpenatan.jParser.loader.JParserLibraryLoaderListener;

public class TestLibLoader {

    public static final String LIB_NAME = "TestLib";

    /*[-JNI;-NATIVE]
        #include "CustomCode.h"
    */

    public static void init(JParserLibraryLoaderListener listener) {
        JParserLibraryLoader.load(LIB_NAME, listener);
    }
}