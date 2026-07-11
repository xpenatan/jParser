package libA;

import com.github.xpenatan.jParser.loader.JParserLibraryLoader;
import com.github.xpenatan.jParser.loader.JParserLibraryLoaderListener;
import com.github.xpenatan.jParser.loader.JParserLibraryLoaderOptions;

public class LibALoader {

    public static final String LIB_NAME = "LibA";

    /*[-JNI;-NATIVE]
        #include "LibACustomCode.h"
    */

    /*[-FFM;-NATIVE]
        #include "LibACustomCode.h"
    */

    /*[-TEAVM_C;-NATIVE]
        #include "LibACustomCode.h"
    */

    public static void init(JParserLibraryLoaderListener listener) {
        init(null, listener);
    }

    public static void init(JParserLibraryLoaderOptions options, JParserLibraryLoaderListener listener) {
        JParserLibraryLoader.load(LIB_NAME, options, listener);
    }
}
