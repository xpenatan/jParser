package libB;

import com.github.xpenatan.jParser.loader.JParserLibraryLoader;
import com.github.xpenatan.jParser.loader.JParserLibraryLoaderListener;

public class LibBLoader {

    public static final String LIB_NAME = "LibB";

    /*[-JNI;-NATIVE]
        #include "LibBCustomCode.h"
    */

    public static void init(JParserLibraryLoaderListener listener) {
        JParserLibraryLoader.load(LIB_NAME, listener);
    }
}