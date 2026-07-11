package com.github.xpenatan.jParser.example.testlib;

import com.github.xpenatan.jParser.loader.JParserLibraryLoader;
import com.github.xpenatan.jParser.loader.JParserLibraryLoaderListener;
import com.github.xpenatan.jParser.loader.JParserLibraryLoaderOptions;

public class TestLibLoader {

    public static final String LIB_NAME = "TestLib";

    /*[-JNI;-NATIVE]
        #include "CustomCode.h"
    */

    /*[-FFM;-NATIVE]
        #include "CustomCode.h"
    */

    public static void init(JParserLibraryLoaderListener listener) {
        init(null, listener);
    }

    public static void init(JParserLibraryLoaderOptions options, JParserLibraryLoaderListener listener) {
        JParserLibraryLoader.load(LIB_NAME, options, listener);
    }
}
