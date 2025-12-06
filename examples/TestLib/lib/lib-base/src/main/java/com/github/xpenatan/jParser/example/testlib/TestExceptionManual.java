package com.github.xpenatan.jParser.example.testlib;

import com.github.xpenatan.jParser.idl.IDLBase;

/*[-IDL_SKIP]*/
public class TestExceptionManual extends IDLBase {

    public TestExceptionManual() {
        long addr = internal_native_create_addr();
        internal_reset(addr, true);
    }

    /*[-JNI;-NATIVE]
        return (jlong)new TestExceptionManual();
    */
    /*[-TEAVM;-NATIVE]
        var testException = new [MODULE].TestExceptionManual();
        return [MODULE].getPointer(testException);
    */
    private static native long internal_native_create_addr();

    public int setDataToNullPointer() {
        return internal_native_setDataToNullPointer(native_address);
    }

    /*[-JNI;-NATIVE]
        TestExceptionManual* nativeObject = (TestExceptionManual*)this_addr;
        return nativeObject->setDataToNullPointer();
    */
    /*[-TEAVM;-NATIVE]
        var nativeObject = [MODULE].wrapPointer(this_addr, [MODULE].TestExceptionManual);
        return nativeObject.setDataToNullPointer();
    */
    private static native int internal_native_setDataToNullPointer(long this_addr);


    public void callJavaMethod(CallbackExceptionManual callback) {
        internal_native_callJavaMethod(native_address, (callback != null ? callback.native_address : 0));
    }

    /*[-JNI;-NATIVE]
        TestExceptionManual* nativeObject = (TestExceptionManual*)this_addr;
        nativeObject->callJavaMethod((CallbackExceptionManual* )callback_addr);
    */
    /*[-TEAVM;-NATIVE]
        var nativeObject = [MODULE].wrapPointer(this_addr, [MODULE].TestExceptionManual);
        try {
            nativeObject.callJavaMethod(callback_addr);
        }
        catch(error) {
            console.log(\"An error occurred from javascript:\", error.message);
            throw new Error(\"Rethrow new Error to java\");
        }
    */
    private static native void internal_native_callJavaMethod(long this_addr, long callback_addr);
}