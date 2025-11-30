package idl;

import com.github.xpenatan.jParser.loader.JParserLibraryLoader;
import com.github.xpenatan.jParser.loader.JParserLibraryLoaderListener;

public class IDLLoader {

    public static final String LIB_NAME = "idl";

    /*[-JNI;-NATIVE]
        #include "IDLCustomCode.h"
    */

    public static void init(JParserLibraryLoaderListener listener) {
        JParserLibraryLoader.load(LIB_NAME, listener);
    }
}