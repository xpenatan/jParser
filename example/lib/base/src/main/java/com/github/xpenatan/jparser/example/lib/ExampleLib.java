package com.github.xpenatan.jparser.example.lib;

import com.github.xpenatan.jparser.loader.JParserLibraryLoader;

public class ExampleLib {

    /*[-teaVM;-ADD]
        @org.teavm.jso.JSFunctor
        public interface OnInitFunction extends org.teavm.jso.JSObject {
            void onInit();
        }
    */

    /*[-teaVM;-REPLACE]
     public static void init(Runnable run) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                OnInitFunction onInitFunction = new OnInitFunction() {
                    @Override
                    public void onInit() {
                        run.run();
                    }
                };
                load(onInitFunction);
            }
        };
        JParserLibraryLoader libraryLoader = new JParserLibraryLoader();
        libraryLoader.load("exampleLib.wasm", runnable);
    }
    */
    public static void init(Runnable run) {
        JParserLibraryLoader libraryLoader = new JParserLibraryLoader();
        libraryLoader.load("exampleLib", run);
    }

    /*[-teaVM;-REPLACE]
        @org.teavm.jso.JSBody(params = { "onInitFunction" }, script = "window.exampleLibOnInit = onInitFunction;")
        private static native void load(OnInitFunction onInitFunction);
    */
    public static native void load();
}