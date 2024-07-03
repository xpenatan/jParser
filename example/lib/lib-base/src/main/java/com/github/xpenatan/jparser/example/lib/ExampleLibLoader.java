package com.github.xpenatan.jparser.example.lib;

import com.github.xpenatan.jparser.loader.JParserLibraryLoader;
import idl.helper.IDLByteArray;

public class ExampleLibLoader {

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
     public static void init(Runnable run) {
        JParserLibraryLoader libraryLoader = new JParserLibraryLoader();
        OnInitFunction onInitFunction = new OnInitFunction() {
            @Override
            public void onInit() {
                run.run();
            }
        };
        setOnLoadInit(onInitFunction);
        libraryLoader.load("exampleLib.wasm");
    }
    */

    public static void init(Runnable run) {
        JParserLibraryLoader libraryLoader = new JParserLibraryLoader();
        libraryLoader.load("exampleLib");

        IDLByteArray test = new IDLByteArray(1);
        test.setValue(0, (byte)1);
        run.run();
    }

    /*[-TEAVM;-REPLACE]
        @org.teavm.jso.JSBody(params = { "onInitFunction" }, script = "window.exampleLibOnInit = onInitFunction;")
        private static native void setOnLoadInit(OnInitFunction onInitFunction);
    */
    /*[-JNI;-REMOVE] */
    public static native void setOnLoadInit();
}