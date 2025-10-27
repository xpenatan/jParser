package com.github.xpenatan.jparser.example.testlib;

import com.github.xpenatan.jparser.idl.IDLBase;

/*[-IDL_SKIP]*/
public class CallbackExceptionManual extends IDLBase {

    /*[-JNI;-NATIVE]
        class CallbackExceptionManualImpl : public CallbackExceptionManual {
            private:
                JNIEnv* env;
                jobject obj;
            public:
                inline static jclass jClassID = 0;
                inline static jmethodID callJava_ID = 0;

                void setupCallback(JNIEnv* env, jobject obj) {
                    this->env = env;
                    this->obj = env->NewGlobalRef(obj);

                    if(CallbackExceptionManualImpl::jClassID == 0) {
                        CallbackExceptionManualImpl::jClassID = (jclass)env->NewGlobalRef(env->GetObjectClass(obj));
                        CallbackExceptionManualImpl::callJava_ID = env->GetMethodID(jClassID, "internal_callJava", "()V");
                    }
                }
                virtual void callJava() const {
                    env->CallVoidMethod(obj, CallbackExceptionManualImpl::callJava_ID);
                }
        };
    */

    /*[-TEAVM;-ADD]
        @org.teavm.jso.JSFunctor
        public interface callJava extends org.teavm.jso.JSObject {
            void callJava();
        }
    */

    public CallbackExceptionManual() {
        long addr = internal_native_create();
        internal_reset(addr, true);
        setupCallbacks();
    }

    /*[-JNI;-NATIVE]
        return (jlong)new CallbackExceptionManualImpl();
    */
    /*[-TEAVM;-NATIVE]
        var nativeObject = new [MODULE].CallbackExceptionManualImpl();
        return [MODULE].getPointer(nativeObject);
    */
    private static native long internal_native_create();

    /*[-TEAVM;-REPLACE_BLOCK]
        {
            callJava callJava = new callJava() {
                @Override
                public void callJava() {
                    internal_callJava();
                }
            };
            internal_native_setupCallbacks((int)native_address, callJava);
        }
    */
    private void setupCallbacks() {
        internal_native_setupCallbacks(native_address);
    }

    /*[-JNI;-NATIVE]
        CallbackExceptionManualImpl* nativeObject = (CallbackExceptionManualImpl*)this_addr;
        nativeObject->setupCallback(env, object);
    */
    /*[-TEAVM;-REPLACE]
        @org.teavm.jso.JSBody(params = { "this_addr", "callJava" }, script = "var nativeObject = [MODULE].wrapPointer(this_addr, [MODULE].CallbackExceptionManualImpl); nativeObject.callJava = callJava;")
        private static native void internal_native_setupCallbacks(int this_addr, callJava callJava);
    */
    private native void internal_native_setupCallbacks(long this_addr);

    // C++ and Javascript/Wasm will call this internal method
    public void internal_callJava() {
    }
}