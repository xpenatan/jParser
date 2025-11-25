package libB;

import com.github.xpenatan.jParser.loader.JParserLibraryLoader;
import com.github.xpenatan.jParser.loader.JParserLibraryLoaderListener;

public class LibBLoader {

    /*[-JNI;-NATIVE]
        #include "LibBCustomCode.h"
    */

    public static final String LIB_NAME = "LibB";

    public static void init(JParserLibraryLoaderListener listener) {
        JParserLibraryLoader.load(LIB_NAME, listener);
    }
}