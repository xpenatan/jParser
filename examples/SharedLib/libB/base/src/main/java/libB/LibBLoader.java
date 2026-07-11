package libB;

import com.github.xpenatan.jParser.loader.JParserLibraryLoader;
import com.github.xpenatan.jParser.loader.JParserLibraryLoaderListener;
import com.github.xpenatan.jParser.loader.JParserLibraryLoaderOptions;

public class LibBLoader {

    /*[-JNI;-NATIVE]
        #include "LibBCustomCode.h"
    */

    /*[-FFM;-NATIVE]
        #include "LibBCustomCode.h"
    */

    /*[-TEAVM_C;-NATIVE]
        #include "LibBCustomCode.h"
    */

    public static final String LIB_NAME = "LibB";

    public static void init(JParserLibraryLoaderListener listener) {
        init(null, listener);
    }

    public static void init(JParserLibraryLoaderOptions options, JParserLibraryLoaderListener listener) {
        JParserLibraryLoader.load(LIB_NAME, options, listener);
    }
}
