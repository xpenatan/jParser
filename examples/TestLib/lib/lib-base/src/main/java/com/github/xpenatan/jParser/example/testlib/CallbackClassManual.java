package com.github.xpenatan.jParser.example.testlib;

import com.github.xpenatan.jParser.idl.IDLBase;

/*[-IDL_SKIP]*/
public class CallbackClassManual extends IDLBase {
    /*[-JNI;-NATIVE]
        #ifdef __ANDROID__
            #include <android/native_window_jni.h>
        #endif
    */

    /*[-JNI;-NATIVE]

        static jmethodID CallbackClassManualImpl_onVoidCallback_ID;
        static jmethodID CallbackClassManualImpl_onIntCallback_ID;
        static jmethodID CallbackClassManualImpl_onFloatCallback_ID;
        static jmethodID CallbackClassManualImpl_onBoolCallback_ID;
        static jmethodID CallbackClassManualImpl_onStringCallback_ID;
        class CallbackClassManualImpl : public CallbackClassManual {
            private:
                JNIEnv* env;
                jobject obj;
            public:

                void setupCallback(JNIEnv* env, jobject obj) {
                    this->env = env;
                    this->obj = env->NewGlobalRef(obj);
                    static jclass jClassID = 0;
                    if(jClassID == 0) {
                        jClassID = (jclass)env->NewGlobalRef(env->GetObjectClass(obj));
                        CallbackClassManualImpl_onVoidCallback_ID = env->GetMethodID(jClassID, "internal_onVoidCallback", "(JJ)V");
                        CallbackClassManualImpl_onIntCallback_ID = env->GetMethodID(jClassID, "internal_onIntCallback", "(II)I");
                        CallbackClassManualImpl_onFloatCallback_ID = env->GetMethodID(jClassID, "internal_onFloatCallback", "(FF)F");
                        CallbackClassManualImpl_onBoolCallback_ID = env->GetMethodID(jClassID, "internal_onBoolCallback", "(Z)Z");
                        CallbackClassManualImpl_onStringCallback_ID = env->GetMethodID(jClassID, "internal_onStringCallback", "(Ljava/lang/String;)V");
                    }
                }
                virtual void onVoidCallback(TestObjectClass& refData, TestObjectClass* pointerData) const {
                    env->CallVoidMethod(obj, CallbackClassManualImpl_onVoidCallback_ID, (jlong)&refData, (jlong)pointerData);
                }
                virtual int onIntCallback(int intValue01, int intValue02) const {
                    return env->CallIntMethod(obj, CallbackClassManualImpl_onIntCallback_ID, intValue01, intValue02);
                }
                virtual float onFloatCallback(float floatValue01, float floatValue02) const {
                    return env->CallFloatMethod(obj, CallbackClassManualImpl_onFloatCallback_ID, floatValue01, floatValue02);
                }
                virtual bool onBoolCallback(bool boolValue01) const {
                    return env->CallBooleanMethod(obj, CallbackClassManualImpl_onBoolCallback_ID, boolValue01);
                }
                virtual void onStringCallback(const char* strValue01) const {
                    jstring jstrBuf = env->NewStringUTF(strValue01);
                    env->CallVoidMethod(obj, CallbackClassManualImpl_onStringCallback_ID, jstrBuf);
                }
        };
    */

    /*[-TEAVM;-ADD]
        @org.teavm.jso.JSFunctor
        public interface onVoidCallback extends org.teavm.jso.JSObject {
            void onVoidCallback(int refData, int pointerData);
        }
    */
    /*[-TEAVM;-ADD]
        @org.teavm.jso.JSFunctor
        public interface onIntCallback extends org.teavm.jso.JSObject {
            int onIntCallback(int intValue01, int intValue02);
        }
    */
    /*[-TEAVM;-ADD]
        @org.teavm.jso.JSFunctor
        public interface onFloatCallback extends org.teavm.jso.JSObject {
            float onFloatCallback(float floatValue01, float floatValue02);
        }
    */
    /*[-TEAVM;-ADD]
        @org.teavm.jso.JSFunctor
        public interface onBoolCallback extends org.teavm.jso.JSObject {
            boolean onBoolCallback(boolean boolValue01);
        }
    */
    /*[-TEAVM;-ADD]
        @org.teavm.jso.JSFunctor
        public interface onStringCallback extends org.teavm.jso.JSObject {
            void onStringCallback(int strValue01);
        }
    */

    public CallbackClassManual() {
        long addr = internal_native_create_addr();
        internal_reset(addr, true);
        setupCallbacks();
    }

    public static long GetAndroidCode() {
        return internal_getAndroidCode();
    }

    /*[-TEAVM;-NATIVE]
        return BigInt(-1);
    */
    /*[-JNI;-NATIVE]
        long long myCode = 0;
        myCode++;
        #ifdef __ANDROID__
            return 1;
        #else
            return 0;
        #endif
    */
    private static native long internal_getAndroidCode();

    /*[-JNI;-NATIVE]
        return (jlong)new CallbackClassManualImpl();
    */
    /*[-TEAVM;-NATIVE]
        var CallbackClassManualImpl = new [MODULE].CallbackClassManualImpl();
        return [MODULE].getPointer(CallbackClassManualImpl);
    */
    private static native long internal_native_create_addr();

    /*[-TEAVM;-REPLACE_BLOCK]
        {
            onVoidCallback onVoidCallback = new onVoidCallback() {
                @Override
                public void onVoidCallback(int refData, int pointerData) {
                    internal_onVoidCallback(refData, pointerData);
                }
            };
            onIntCallback onIntCallback = new onIntCallback() {
                @Override
                public int onIntCallback(int intValue01, int intValue02) {
                    return internal_onIntCallback(intValue01, intValue02);
                }
            };
            onFloatCallback onFloatCallback = new onFloatCallback() {
                @Override
                public float onFloatCallback(float floatValue01, float floatValue02) {
                    return internal_onFloatCallback(floatValue01, floatValue02);
                }
            };
            onBoolCallback onBoolCallback = new onBoolCallback() {
                @Override
                public boolean onBoolCallback(boolean boolValue01) {
                    return internal_onBoolCallback(boolValue01);
                }
            };
            onStringCallback onStringCallback = new onStringCallback() {
                @Override
                public void onStringCallback(int strValue01) {
                    internal_onStringCallback(gen.com.github.xpenatan.jparser.idl.helper.IDLUtils.getJSString(strValue01));
                }
            };
            internal_native_setupCallbacks((int)native_address, onVoidCallback, onIntCallback, onFloatCallback, onBoolCallback, onStringCallback);
        }
    */
    private void setupCallbacks() {
        internal_native_setupCallbacks(native_address);
    }

    /*[-JNI;-NATIVE]
        CallbackClassManualImpl* nativeObject = (CallbackClassManualImpl*)this_addr;
        nativeObject->setupCallback(env, object);
    */
    /*[-TEAVM;-REPLACE]
        @org.teavm.jso.JSBody(params = { "this_addr", "onVoidCallback", "onIntCallback", "onFloatCallback", "onBoolCallback", "onStringCallback" }, script = "var CallbackClassManualImpl = [MODULE].wrapPointer(this_addr, [MODULE].CallbackClassManualImpl); CallbackClassManualImpl.onVoidCallback = onVoidCallback; CallbackClassManualImpl.onIntCallback = onIntCallback; CallbackClassManualImpl.onFloatCallback = onFloatCallback; CallbackClassManualImpl.onBoolCallback = onBoolCallback; CallbackClassManualImpl.onStringCallback = onStringCallback;")
        private static native void internal_native_setupCallbacks(int this_addr, onVoidCallback onVoidCallback, onIntCallback onIntCallback, onFloatCallback onFloatCallback, onBoolCallback onBoolCallback, onStringCallback onStringCallback);
    */
    private native void internal_native_setupCallbacks(long this_addr);

    public void internal_onVoidCallback(long refData, long pointerData) {
    }

    public int internal_onIntCallback(int intValue01, int intValue02) {
        return 0;
    }

    public float internal_onFloatCallback(float floatValue01, float floatValue02) {
        return 0;
    }

    public boolean internal_onBoolCallback(boolean boolValue01) {
        return false;
    }

    public void internal_onStringCallback(String strValue01) {
    }
}