package lib.test;

import com.github.xpenatan.jparser.loader.JParserLibraryLoader;

public class TestLibLoader {

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
        libraryLoader.load("test.wasm", isSuccess -> {});
    }
    */

    public static void init(Runnable onSuccess) {
        JParserLibraryLoader libraryLoader = new JParserLibraryLoader();
        libraryLoader.load("test", isSuccess -> {
            if(isSuccess) {
                onSuccess.run();
            }
        });
    }

    /*[-TEAVM;-REPLACE]
        @org.teavm.jso.JSBody(params = { "onInitFunction" }, script = "window.exampleLibOnInit = onInitFunction;")
        private static native void setOnLoadInit(OnInitFunction onInitFunction);
    */
    /*[-JNI;-REMOVE] */
    public static native void setOnLoadInit();
}