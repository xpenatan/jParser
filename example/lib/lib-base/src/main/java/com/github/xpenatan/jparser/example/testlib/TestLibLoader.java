package com.github.xpenatan.jparser.example.testlib;

import com.github.xpenatan.jparser.loader.JParserLibraryLoader;
import com.github.xpenatan.jparser.loader.JParserLibraryLoaderListener;

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
     public static void init(JParserLibraryLoaderListener listener) {
        JParserLibraryLoader libraryLoader = new JParserLibraryLoader();
        setOnLoadInit(() -> listener.onLoad(true, null));
        libraryLoader.load(LIB_NAME + ".wasm", (isSuccess, e) -> {
            if(!isSuccess) {
                listener.onLoad(false, e);
            }
        });
    }
    */

    public static void init(JParserLibraryLoaderListener listener) {
        JParserLibraryLoader libraryLoader = new JParserLibraryLoader();
        libraryLoader.load(LIB_NAME, listener);
    }

    /*[-TEAVM;-REPLACE]
        @org.teavm.jso.JSBody(params = { "onInitFunction" }, script = "window.TestLibOnInit = onInitFunction;")
        private static native void setOnLoadInit(OnInitFunction onInitFunction);
    */
    /*[-JNI;-REMOVE] */
    public static native void setOnLoadInit();
}