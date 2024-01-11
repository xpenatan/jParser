package idl.helper;

import idl.IDLBase;

public class IDLString extends IDLBase {

    public static IDLString TMP_1 = new IDLString();
    public static IDLString TMP_2 = new IDLString();

    public static void disposeTEMP() {
        TMP_1.dispose();
        TMP_2.dispose();
    }

    public IDLString() {
    }

    public String c_str() {
        String text = c_strNATIVE(getCPointer());
        return text;
    }

    /*[-C++;-NATIVE]
        IDLString* nativeObject = (IDLString*)this_addr;
        const char* str = nativeObject->c_str();
        jstring jstrBuf = env->NewStringUTF(str);
        return jstrBuf;
    */
    /*[-teaVM;-NATIVE]
        var jsObj = [MODULE].wrapPointer(this_addr, [MODULE].IDLString);
        var returnedJSObj = jsObj.c_str();
        return returnedJSObj;
    */
    private static native String c_strNATIVE(long this_addr);
}