package com.github.xpenatan.jparser.example.testlib;

import com.github.xpenatan.jparser.loader.JParserLibraryLoader;
import com.github.xpenatan.jparser.loader.JParserLibraryLoaderListener;

public class TestLibLoader {

    public static final String LIB_NAME = "TestLib";

    /*[-JNI;-NATIVE]
        #include "CustomCode.h"
    */

    public static void init(JParserLibraryLoaderListener listener) {
        JParserLibraryLoader.load(LIB_NAME, listener);
    }
}