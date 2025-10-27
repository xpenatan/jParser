package idl.helper;

import com.github.xpenatan.jparser.idl.IDLBase;

public class IDLString extends IDLBase {

    public static final IDLString NULL = native_new();

    /**
     * @return An empty instance without a native address
     */
    public static IDLString native_new() {
        return new IDLString((byte) 1, (char) 1);
    }

    protected IDLString(byte b, char c) {}

    public IDLString() {
    }

    public String c_str() {
        String text = internal_native_c_str(native_address);
        return text;
    }

    /*[-JNI;-NATIVE]
        IDLString* nativeObject = (IDLString*)this_addr;
        const char* str = nativeObject->c_str();
        jstring jstrBuf = env->NewStringUTF(str);
        return jstrBuf;
    */
    /*[-TEAVM;-NATIVE]
        var jsObj = [MODULE].wrapPointer(this_addr, [MODULE].IDLString);
        var returnedJSObj = jsObj.c_str();
        return returnedJSObj;
    */
    private static native String internal_native_c_str(long this_addr);

    public String data() {
        String text = internal_native_data(native_address);
        return text;
    }

    /*[-JNI;-NATIVE]
        IDLString* nativeObject = (IDLString*)this_addr;
        const char* str = nativeObject->data();
        jstring jstrBuf = env->NewStringUTF(str);
        return jstrBuf;
    */
    /*[-TEAVM;-NATIVE]
        var jsObj = [MODULE].wrapPointer(this_addr, [MODULE].IDLString);
        var returnedJSObj = jsObj.data();
        return returnedJSObj;
    */
    private static native String internal_native_data(long this_addr);
}