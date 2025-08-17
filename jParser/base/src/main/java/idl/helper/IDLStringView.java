package idl.helper;

import idl.IDLBase;

public class IDLStringView extends IDLBase {

    public static final IDLStringView NULL = createInstance();
    public static IDLStringView TMP_1 = new IDLStringView();
    public static IDLStringView TMP_2 = new IDLStringView();

    public static void disposeTEMP() {
        TMP_1.dispose();
        TMP_2.dispose();
    }

    /**
     * @return An empty instance without a native address
     */
    public static IDLStringView createInstance() {
        return new IDLStringView((byte) 1, (char) 1);
    }

    public IDLStringView() {
    }

    protected IDLStringView(byte b, char c) {}

    public String data() {
        String text = internal_native_data(native_address);
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