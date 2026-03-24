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

    /*[-FFM;-NATIVE]
        typedef void (*fp_CCMImpl_onVoidCallback)(int64_t, int64_t);
        typedef int32_t (*fp_CCMImpl_onIntCallback)(int32_t, int32_t);
        typedef float (*fp_CCMImpl_onFloatCallback)(float, float);
        typedef int32_t (*fp_CCMImpl_onBoolCallback)(int32_t);
        typedef void (*fp_CCMImpl_onStringCallback)(const char*);
        class CallbackClassManualImpl : public CallbackClassManual {
        private:
            fp_CCMImpl_onVoidCallback onVoidCallback_ptr;
            fp_CCMImpl_onIntCallback onIntCallback_ptr;
            fp_CCMImpl_onFloatCallback onFloatCallback_ptr;
            fp_CCMImpl_onBoolCallback onBoolCallback_ptr;
            fp_CCMImpl_onStringCallback onStringCallback_ptr;
        public:
            void setupCallback(fp_CCMImpl_onVoidCallback a, fp_CCMImpl_onIntCallback b, fp_CCMImpl_onFloatCallback c, fp_CCMImpl_onBoolCallback d, fp_CCMImpl_onStringCallback e) {
                this->onVoidCallback_ptr = a;
                this->onIntCallback_ptr = b;
                this->onFloatCallback_ptr = c;
                this->onBoolCallback_ptr = d;
                this->onStringCallback_ptr = e;
            }
            virtual void onVoidCallback(TestObjectClass& refData, TestObjectClass* pointerData) const {
                onVoidCallback_ptr((int64_t)&refData, (int64_t)pointerData);
            }
            virtual int onIntCallback(int intValue01, int intValue02) const {
                return (int)onIntCallback_ptr(intValue01, intValue02);
            }
            virtual float onFloatCallback(float floatValue01, float floatValue02) const {
                return (float)onFloatCallback_ptr(floatValue01, floatValue02);
            }
            virtual bool onBoolCallback(bool boolValue01) const {
                return (bool)onBoolCallback_ptr(boolValue01);
            }
            virtual void onStringCallback(const char* strValue01) const {
                onStringCallback_ptr(strValue01);
            }
        };

        extern "C" {
        FFM_EXPORT int64_t jparser_com_github_xpenatan_jParser_example_testlib_CallbackClassManual_internal_1native_1create_1addr__(void) {
            return (int64_t)new CallbackClassManualImpl();
        }
        FFM_EXPORT int64_t jparser_com_github_xpenatan_jParser_example_testlib_CallbackClassManual_internal_1getAndroidCode__(void) {
            long long myCode = 0;
            myCode++;
            #ifdef __ANDROID__
                return 1;
            #else
                return 0;
            #endif
        }
        FFM_EXPORT void jparser_com_github_xpenatan_jParser_example_testlib_CallbackClassManual_internal_1native_1setupCallbacks__JJJJJJ(int64_t this_addr, int64_t onVoidCallback_fp, int64_t onIntCallback_fp, int64_t onFloatCallback_fp, int64_t onBoolCallback_fp, int64_t onStringCallback_fp) {
            CallbackClassManualImpl* nativeObject = (CallbackClassManualImpl*)this_addr;
            nativeObject->setupCallback((fp_CCMImpl_onVoidCallback)onVoidCallback_fp, (fp_CCMImpl_onIntCallback)onIntCallback_fp, (fp_CCMImpl_onFloatCallback)onFloatCallback_fp, (fp_CCMImpl_onBoolCallback)onBoolCallback_fp, (fp_CCMImpl_onStringCallback)onStringCallback_fp);
        }
        }
    */

    /*[-FFM;-ADD]
        private void internal_ffm_onStringCallback(java.lang.foreign.MemorySegment seg) {
            String str = seg.reinterpret(Long.MAX_VALUE).getString(0);
            internal_onStringCallback(str);
        }
    */

    /*[-FFM;-ADD]
        private static final class FFMHandles {
            private static final java.lang.foreign.SymbolLookup LOOKUP = java.lang.foreign.SymbolLookup.loaderLookup();
            private static final java.lang.foreign.Linker LINKER = java.lang.foreign.Linker.nativeLinker();
            static final java.lang.invoke.MethodHandle create_addr = LINKER.downcallHandle(LOOKUP.find("jparser_com_github_xpenatan_jParser_example_testlib_CallbackClassManual_internal_1native_1create_1addr__").orElseThrow(), java.lang.foreign.FunctionDescriptor.of(java.lang.foreign.ValueLayout.JAVA_LONG));
            static final java.lang.invoke.MethodHandle getAndroidCode = LINKER.downcallHandle(LOOKUP.find("jparser_com_github_xpenatan_jParser_example_testlib_CallbackClassManual_internal_1getAndroidCode__").orElseThrow(), java.lang.foreign.FunctionDescriptor.of(java.lang.foreign.ValueLayout.JAVA_LONG));
            static final java.lang.invoke.MethodHandle setupCallbacks = LINKER.downcallHandle(LOOKUP.find("jparser_com_github_xpenatan_jParser_example_testlib_CallbackClassManual_internal_1native_1setupCallbacks__JJJJJJ").orElseThrow(), java.lang.foreign.FunctionDescriptor.ofVoid(java.lang.foreign.ValueLayout.JAVA_LONG, java.lang.foreign.ValueLayout.JAVA_LONG, java.lang.foreign.ValueLayout.JAVA_LONG, java.lang.foreign.ValueLayout.JAVA_LONG, java.lang.foreign.ValueLayout.JAVA_LONG, java.lang.foreign.ValueLayout.JAVA_LONG));
        }
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
    /*[-FFM;-REPLACE]
        private static long internal_getAndroidCode() {
            try { return (long) FFMHandles.getAndroidCode.invokeExact(); }
            catch(Throwable e) { throw new RuntimeException(e); }
        }
    */
    private static native long internal_getAndroidCode();

    /*[-JNI;-NATIVE]
        return (jlong)new CallbackClassManualImpl();
    */
    /*[-TEAVM;-NATIVE]
        var CallbackClassManualImpl = new [MODULE].CallbackClassManualImpl();
        return [MODULE].getPointer(CallbackClassManualImpl);
    */
    /*[-FFM;-REPLACE]
        private static long internal_native_create_addr() {
            try { return (long) FFMHandles.create_addr.invokeExact(); }
            catch(Throwable e) { throw new RuntimeException(e); }
        }
    */
    private static native long internal_native_create_addr();

    /*[-FFM;-REPLACE_BLOCK]
        {
            try {
                java.lang.invoke.MethodHandle mh_void = java.lang.invoke.MethodHandles.lookup().findVirtual(CallbackClassManual.class, "internal_onVoidCallback", java.lang.invoke.MethodType.methodType(void.class, long.class, long.class)).bindTo(this);
                java.lang.foreign.MemorySegment stub_void = java.lang.foreign.Linker.nativeLinker().upcallStub(mh_void, java.lang.foreign.FunctionDescriptor.ofVoid(java.lang.foreign.ValueLayout.JAVA_LONG, java.lang.foreign.ValueLayout.JAVA_LONG), java.lang.foreign.Arena.ofAuto());
                java.lang.invoke.MethodHandle mh_int = java.lang.invoke.MethodHandles.lookup().findVirtual(CallbackClassManual.class, "internal_onIntCallback", java.lang.invoke.MethodType.methodType(int.class, int.class, int.class)).bindTo(this);
                java.lang.foreign.MemorySegment stub_int = java.lang.foreign.Linker.nativeLinker().upcallStub(mh_int, java.lang.foreign.FunctionDescriptor.of(java.lang.foreign.ValueLayout.JAVA_INT, java.lang.foreign.ValueLayout.JAVA_INT, java.lang.foreign.ValueLayout.JAVA_INT), java.lang.foreign.Arena.ofAuto());
                java.lang.invoke.MethodHandle mh_float = java.lang.invoke.MethodHandles.lookup().findVirtual(CallbackClassManual.class, "internal_onFloatCallback", java.lang.invoke.MethodType.methodType(float.class, float.class, float.class)).bindTo(this);
                java.lang.foreign.MemorySegment stub_float = java.lang.foreign.Linker.nativeLinker().upcallStub(mh_float, java.lang.foreign.FunctionDescriptor.of(java.lang.foreign.ValueLayout.JAVA_FLOAT, java.lang.foreign.ValueLayout.JAVA_FLOAT, java.lang.foreign.ValueLayout.JAVA_FLOAT), java.lang.foreign.Arena.ofAuto());
                java.lang.invoke.MethodHandle mh_bool = java.lang.invoke.MethodHandles.lookup().findVirtual(CallbackClassManual.class, "internal_onBoolCallback", java.lang.invoke.MethodType.methodType(boolean.class, boolean.class)).bindTo(this);
                java.lang.foreign.MemorySegment stub_bool = java.lang.foreign.Linker.nativeLinker().upcallStub(mh_bool, java.lang.foreign.FunctionDescriptor.of(java.lang.foreign.ValueLayout.JAVA_BOOLEAN, java.lang.foreign.ValueLayout.JAVA_BOOLEAN), java.lang.foreign.Arena.ofAuto());
                java.lang.invoke.MethodHandle mh_string = java.lang.invoke.MethodHandles.lookup().findVirtual(CallbackClassManual.class, "internal_ffm_onStringCallback", java.lang.invoke.MethodType.methodType(void.class, java.lang.foreign.MemorySegment.class)).bindTo(this);
                java.lang.foreign.MemorySegment stub_string = java.lang.foreign.Linker.nativeLinker().upcallStub(mh_string, java.lang.foreign.FunctionDescriptor.ofVoid(java.lang.foreign.ValueLayout.ADDRESS), java.lang.foreign.Arena.ofAuto());
                internal_native_setupCallbacks(native_address, stub_void.address(), stub_int.address(), stub_float.address(), stub_bool.address(), stub_string.address());
            } catch(Throwable e) {
                throw new RuntimeException(e);
            }
        }
    */
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
    /*[-FFM;-REPLACE]
        private static void internal_native_setupCallbacks(long this_addr, long onVoidCallback_fp, long onIntCallback_fp, long onFloatCallback_fp, long onBoolCallback_fp, long onStringCallback_fp) {
            try { FFMHandles.setupCallbacks.invokeExact(this_addr, onVoidCallback_fp, onIntCallback_fp, onFloatCallback_fp, onBoolCallback_fp, onStringCallback_fp); }
            catch(Throwable e) { throw new RuntimeException(e); }
        }
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