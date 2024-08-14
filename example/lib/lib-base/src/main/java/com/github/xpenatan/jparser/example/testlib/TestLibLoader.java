package com.github.xpenatan.jparser.example.testlib;

import com.github.xpenatan.jparser.loader.JParserLibraryLoader;

public class TestLibLoader {

    public static final String LIB_NAME = "TestLib";

    /*[-JNI;-NATIVE]
        #include "CustomCode.h"
    */

    /*[-TEAVM;-ADD]
        @org.teavm.jso.JSFunctor
        public interface OnInitFunction extends org.teavm.jso.JSObject {
            void onInit();
        }
    */

    /*[-TEAVM;-REPLACE]
     public static void init(Runnable onSuccess) {
        JParserLibraryLoader libraryLoader = new JParserLibraryLoader();
        OnInitFunction onInitFunction = () -> onSuccess.run();
        setOnLoadInit(onInitFunction);
        libraryLoader.load(LIB_NAME + ".wasm", isSuccess -> {});
    }
    */

    public static void init(Runnable onSuccess) {
        JParserLibraryLoader libraryLoader = new JParserLibraryLoader();
        libraryLoader.load(LIB_NAME, isSuccess -> {
            if(isSuccess) {
                onSuccess.run();
            }
        });
    }

    /*[-TEAVM;-REPLACE]
        @org.teavm.jso.JSBody(params = { "onInitFunction" }, script = "window.TestLibOnInit = onInitFunction;")
        private static native void setOnLoadInit(OnInitFunction onInitFunction);
    */
    /*[-JNI;-REMOVE] */
    public static native void setOnLoadInit();
}