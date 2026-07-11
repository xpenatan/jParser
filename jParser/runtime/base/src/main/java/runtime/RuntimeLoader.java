package runtime;

import com.github.xpenatan.jParser.loader.JParserLibraryLoader;
import com.github.xpenatan.jParser.loader.JParserLibraryLoaderListener;
import com.github.xpenatan.jParser.loader.JParserLibraryLoaderOptions;

public class RuntimeLoader {

    public static final String LIB_NAME = "runtime";

    /*[-JNI;-NATIVE]
        #include "IDLCustomCode.h"
    */

    /*[-FFM;-NATIVE]
        #include "IDLCustomCode.h"
    */

    public static void init(JParserLibraryLoaderListener listener) {
        init(null, listener);
    }

    public static void init(JParserLibraryLoaderOptions options, JParserLibraryLoaderListener listener) {
        JParserLibraryLoader.load(LIB_NAME, options, listener);
    }
}
