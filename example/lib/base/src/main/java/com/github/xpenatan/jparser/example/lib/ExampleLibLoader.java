package com.github.xpenatan.jparser.example.lib;

import com.github.xpenatan.jparser.loader.JParserLibraryLoader;
import idl.helper.ByteArray;

public class ExampleLibLoader {

    /*[-C++;-NATIVE]
        #include "ReturnClass.h"
        #include "ParentClass.h"
        #include "OperatorClass.h"
        #include "NormalClass.h"
        #include "InterfaceClass.h"
        #include "DefaultInterface.h"
        #include "subpackage/ParamData.h"
    */


    /*[-teaVM;-ADD]
        @org.teavm.jso.JSFunctor
        public interface OnInitFunction extends org.teavm.jso.JSObject {
            void onInit();
        }
    */

    /*[-teaVM;-REPLACE]
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

        ByteArray test = new ByteArray(1);
        test.setValue(0, (byte)1);
        run.run();
    }

    /*[-teaVM;-REPLACE]
        @org.teavm.jso.JSBody(params = { "onInitFunction" }, script = "window.exampleLibOnInit = onInitFunction;")
        private static native void setOnLoadInit(OnInitFunction onInitFunction);
    */
    /*[-C++;-REMOVE] */
    public static native void setOnLoadInit();
}