package idl.helper;

import idl.IDLBase;

public class IDLStringView extends IDLBase {

    public static IDLStringView TMP_EMPTY_1 = new IDLStringView((byte)0, '0');

    public static IDLStringView TMP_1 = new IDLStringView();
    public static IDLStringView TMP_2 = new IDLStringView();

    public static void disposeTEMP() {
        TMP_1.dispose();
        TMP_2.dispose();
    }

    public IDLStringView() {
    }

    public IDLStringView(byte b, char c) {}

    public String data() {
        String text = internal_native_data(getCPointer());
        return text;
    }

    /*[-JNI;-NATIVE]
        IDLStringView* nativeObject = (IDLStringView*)this_addr;
        const char* str = nativeObject->data();
        jstring jstrBuf = env->NewStringUTF(str);
        return jstrBuf;
    */
    /*[-TEAVM;-NATIVE]
        var jsObj = [MODULE].wrapPointer(this_addr, [MODULE].IDLStringView);
        var returnedJSObj = jsObj.data();
        return returnedJSObj;
    */
    private static native String internal_native_data(long this_addr);
}