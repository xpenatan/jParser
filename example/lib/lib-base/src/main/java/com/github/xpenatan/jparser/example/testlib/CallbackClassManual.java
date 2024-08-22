package com.github.xpenatan.jparser.example.testlib;
import idl.IDLBase;

/*[-IDL_SKIP]*/
public class CallbackClassManual extends IDLBase {

    /*[-JNI;-NATIVE]
        static jclass CallbackClassManual_CLASS = 0;
        static jmethodID onVoidCallback_ID = 0;
        static jmethodID onIntCallback_ID = 0;
        static jmethodID onFloatCallback_ID = 0;
        static jmethodID onBoolCallback_ID = 0;
        static jmethodID onStringCallback_ID = 0;

        class CallbackClassManual_NATIVE : public CallbackClassManual {
            private:
                JNIEnv* env;
                jobject obj;
            public:
                CallbackClassManual_NATIVE( JNIEnv* env, jobject obj ) {
                    this->env = env;
                    this->obj = obj;
                }
                virtual void onVoidCallback(TestObjectClass& refData, TestObjectClass* pointerData) const {
                    env->CallVoidMethod(obj, onVoidCallback_ID, (jlong)&refData, (jlong)pointerData);
                }
                virtual int onIntCallback(int intValue01, int intValue02) const {
                    return env->CallIntMethod(obj, onIntCallback_ID, intValue01, intValue02);
                }
                virtual float onFloatCallback(float floatValue01, float floatValue02) const {
                    return env->CallFloatMethod(obj, onFloatCallback_ID, floatValue01, floatValue02);
                }
                virtual bool onBoolCallback(bool boolValue01) const {
                    return env->CallBooleanMethod(obj, onBoolCallback_ID, boolValue01);
                }
                virtual void onStringCallback(const char* strValue01) const {
                    jstring jstrBuf = env->NewStringUTF(strValue01);
                    env->CallVoidMethod(obj, onStringCallback_ID, jstrBuf);
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

    /*[-TEAVM;-REPLACE]
        public CallbackClassManual() {
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
                    internal_onStringCallback(IDLBase.getJSString(strValue01));
                }
            };
            int pointer = createNative(onVoidCallback, onIntCallback, onFloatCallback, onBoolCallback, onStringCallback);
            initNative(pointer, true);
        }
    */
    public CallbackClassManual() {
        long addr = createNATIVE();
        initNative(addr, true);
    }

    /*[-JNI;-NATIVE]
        if(CallbackClassManual_CLASS == 0) {
            CallbackClassManual_CLASS = (jclass)env->NewGlobalRef(env->GetObjectClass(object));
            onVoidCallback_ID = env->GetMethodID(CallbackClassManual_CLASS, "internal_onVoidCallback", "(JJ)V");
            onIntCallback_ID = env->GetMethodID(CallbackClassManual_CLASS, "internal_onIntCallback", "(II)I");
            onFloatCallback_ID = env->GetMethodID(CallbackClassManual_CLASS, "internal_onFloatCallback", "(FF)F");
            onBoolCallback_ID = env->GetMethodID(CallbackClassManual_CLASS, "internal_onBoolCallback", "(Z)Z");
            onStringCallback_ID = env->GetMethodID(CallbackClassManual_CLASS, "internal_onStringCallback", "(Ljava/lang/String;)V");
        }
        return (jlong)new CallbackClassManual_NATIVE(env, env->NewGlobalRef(object));
    */
    /*[-TEAVM;-REPLACE]
        @org.teavm.jso.JSBody(params = { "onVoidCallback", "onIntCallback", "onFloatCallback", "onBoolCallback", "onStringCallback" }, script = "var CallbackClassManualImpl = new [MODULE].CallbackClassManualImpl(); CallbackClassManualImpl.onVoidCallback = onVoidCallback; CallbackClassManualImpl.onIntCallback = onIntCallback; CallbackClassManualImpl.onFloatCallback = onFloatCallback; CallbackClassManualImpl.onBoolCallback = onBoolCallback; CallbackClassManualImpl.onStringCallback = onStringCallback; return [MODULE].getPointer(CallbackClassManualImpl);")
        private static native int createNative(onVoidCallback onVoidCallback, onIntCallback onIntCallback, onFloatCallback onFloatCallback, onBoolCallback onBoolCallback, onStringCallback onStringCallback);
    */
    private native long createNATIVE();

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