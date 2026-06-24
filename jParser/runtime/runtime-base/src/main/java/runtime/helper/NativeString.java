package runtime.helper;

import com.github.xpenatan.jParser.api.NativeObject;

public class NativeString extends NativeObject {

    public static final NativeString NULL = native_new();

    /**
     * @return An empty instance without a native address
     */
    public static NativeString native_new() {
        return new NativeString((byte) 1, (char) 1);
    }

    protected NativeString(byte b, char c) {}

    public NativeString() {
    }

    public String c_str() {
        String text = internal_native_c_str(native_address);
        return text;
    }

    /*[-JNI;-NATIVE]
        NativeString* nativeObject = (NativeString*)this_addr;
        const char* str = nativeObject->c_str();
        jstring jstrBuf = env->NewStringUTF(str);
        return jstrBuf;
    */
    /*[-TEAVM;-NATIVE]
        var jsObj = [MODULE].wrapPointer(this_addr, [MODULE].NativeString);
        var returnedJSObj = jsObj.c_str();
        return returnedJSObj;
    */
    /*[-FFM;-NATIVE]
        NativeString* nativeObject = (NativeString*)this_addr;
        return nativeObject->c_str();
    */
    /*[-TEAVM_C;-NATIVE]
        NativeString* nativeObject = (NativeString*)this_addr;
        return nativeObject->c_str();
    */
    private static native String internal_native_c_str(long this_addr);

    public String data() {
        String text = internal_native_data(native_address);
        return text;
    }

    /*[-JNI;-NATIVE]
        NativeString* nativeObject = (NativeString*)this_addr;
        const char* str = nativeObject->data();
        jstring jstrBuf = env->NewStringUTF(str);
        return jstrBuf;
    */
    /*[-TEAVM;-NATIVE]
        var jsObj = [MODULE].wrapPointer(this_addr, [MODULE].NativeString);
        var returnedJSObj = jsObj.data();
        return returnedJSObj;
    */
    /*[-FFM;-NATIVE]
        NativeString* nativeObject = (NativeString*)this_addr;
        return nativeObject->data();
    */
    /*[-TEAVM_C;-NATIVE]
        NativeString* nativeObject = (NativeString*)this_addr;
        return nativeObject->data();
    */
    private static native String internal_native_data(long this_addr);
}
