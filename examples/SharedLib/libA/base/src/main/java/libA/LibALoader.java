package libA;

import com.github.xpenatan.jParser.loader.JParserLibraryLoader;
import com.github.xpenatan.jParser.loader.JParserLibraryLoaderListener;

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
