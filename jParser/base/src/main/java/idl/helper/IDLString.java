package idl.helper;

import idl.IDLBase;

public class IDLString extends IDLBase {

    public static IDLString TMP_EMPTY_1 = new IDLString((byte)0, '0');

    public static IDLString TMP_1 = new IDLString();
    public static IDLString TMP_2 = new IDLString();

    public static void disposeTEMP() {
        TMP_1.dispose();
        TMP_2.dispose();
    }

    public IDLString() {
    }

    public IDLString(byte b, char c) {}

    public String c_str() {
        String text = internal_native_c_str(getNativeData().getCPointer());
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
        String text = internal_native_data(getNativeData().getCPointer());
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