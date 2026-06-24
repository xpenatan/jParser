package runtime;

import com.github.xpenatan.jParser.loader.JParserLibraryLoader;
import com.github.xpenatan.jParser.loader.JParserLibraryLoaderListener;

public class RuntimeLoader {

    public static final String LIB_NAME = "runtime";

    /*[-JNI;-NATIVE]
        #include "IDLCustomCode.h"
    */

    /*[-FFM;-NATIVE]
        #include "IDLCustomCode.h"
    */

    /*[-TEAVM_C;-REPLACE_BLOCK]
    {
        if(listener != null) {
            listener.onLoad(true, null);
        }
    }
    */
    public static void init(JParserLibraryLoaderListener listener) {
        JParserLibraryLoader.load(LIB_NAME, listener);
    }
}
