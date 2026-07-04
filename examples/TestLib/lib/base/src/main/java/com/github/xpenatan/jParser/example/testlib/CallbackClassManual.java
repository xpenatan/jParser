package com.github.xpenatan.jParser.example.testlib;

import com.github.xpenatan.jParser.api.NativeObject;

/*[-IDL_SKIP]*/
public class CallbackClassManual extends NativeObject {
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
    /*[-TEAVM_C;-NATIVE]
        typedef void (*fp_CCMImpl_onVoidCallback)(int32_t, int64_t, int64_t);
        typedef int32_t (*fp_CCMImpl_onIntCallback)(int32_t, int32_t, int32_t);
        typedef float (*fp_CCMImpl_onFloatCallback)(int32_t, float, float);
        typedef int32_t (*fp_CCMImpl_onBoolCallback)(int32_t, int32_t);
        typedef void (*fp_CCMImpl_onStringCallback)(int32_t, void*);
        class CallbackClassManualImpl : public CallbackClassManual {
        private:
            int32_t teavmc_callback_id = -1;
            fp_CCMImpl_onVoidCallback onVoidCallback_ptr = nullptr;
            fp_CCMImpl_onIntCallback onIntCallback_ptr = nullptr;
            fp_CCMImpl_onFloatCallback onFloatCallback_ptr = nullptr;
            fp_CCMImpl_onBoolCallback onBoolCallback_ptr = nullptr;
            fp_CCMImpl_onStringCallback onStringCallback_ptr = nullptr;
        public:
            void setupCallback(int32_t callback_id, fp_CCMImpl_onVoidCallback a, fp_CCMImpl_onIntCallback b, fp_CCMImpl_onFloatCallback c, fp_CCMImpl_onBoolCallback d, fp_CCMImpl_onStringCallback e) {
                this->teavmc_callback_id = callback_id;
                this->onVoidCallback_ptr = a;
                this->onIntCallback_ptr = b;
                this->onFloatCallback_ptr = c;
                this->onBoolCallback_ptr = d;
                this->onStringCallback_ptr = e;
            }
            virtual void onVoidCallback(TestObjectClass& refData, TestObjectClass* pointerData) const {
                if(onVoidCallback_ptr != nullptr) onVoidCallback_ptr(teavmc_callback_id, (int64_t)&refData, (int64_t)pointerData);
            }
            virtual int onIntCallback(int intValue01, int intValue02) const {
                if(onIntCallback_ptr == nullptr) return 0;
                return (int)onIntCallback_ptr(teavmc_callback_id, intValue01, intValue02);
            }
            virtual float onFloatCallback(float floatValue01, float floatValue02) const {
                if(onFloatCallback_ptr == nullptr) return 0;
                return (float)onFloatCallback_ptr(teavmc_callback_id, floatValue01, floatValue02);
            }
            virtual bool onBoolCallback(bool boolValue01) const {
                if(onBoolCallback_ptr == nullptr) return false;
                return (bool)onBoolCallback_ptr(teavmc_callback_id, boolValue01);
            }
            virtual void onStringCallback(const char* strValue01) const {
                if(onStringCallback_ptr != nullptr) onStringCallback_ptr(teavmc_callback_id, (void*)strValue01);
            }
        };

        extern "C" {
        TEAVMC_EXPORT void teavmc_CallbackClassManual_setupCallbacks(int64_t this_addr, int32_t callback_id, fp_CCMImpl_onVoidCallback onVoidCallback_fp, fp_CCMImpl_onIntCallback onIntCallback_fp, fp_CCMImpl_onFloatCallback onFloatCallback_fp, fp_CCMImpl_onBoolCallback onBoolCallback_fp, fp_CCMImpl_onStringCallback onStringCallback_fp) {
            CallbackClassManualImpl* nativeObject = (CallbackClassManualImpl*)this_addr;
            nativeObject->setupCallback(callback_id, onVoidCallback_fp, onIntCallback_fp, onFloatCallback_fp, onBoolCallback_fp, onStringCallback_fp);
        }
        }
    */

    /*[-FFM;-ADD]
        private java.lang.foreign.Arena ffm_upcallArena;
    */
    /*[-FFM;-ADD]
        private java.lang.foreign.MemorySegment ffm_stub_void;
    */
    /*[-FFM;-ADD]
        private java.lang.foreign.MemorySegment ffm_stub_int;
    */
    /*[-FFM;-ADD]
        private java.lang.foreign.MemorySegment ffm_stub_float;
    */
    /*[-FFM;-ADD]
        private java.lang.foreign.MemorySegment ffm_stub_bool;
    */
    /*[-FFM;-ADD]
        private java.lang.foreign.MemorySegment ffm_stub_string;
    */
    /*[-FFM;-ADD]
        private void ffm_releaseCallbacks() {
            java.lang.foreign.Arena arena = ffm_upcallArena;
            ffm_stub_void = null;
            ffm_stub_int = null;
            ffm_stub_float = null;
            ffm_stub_bool = null;
            ffm_stub_string = null;
            ffm_upcallArena = null;
            if(arena != null) {
                try {
                    arena.close();
                } catch(Exception ignored) {
                }
            }
        }
    */
    /*[-FFM;-ADD]
        private void internal_ffm_onStringCallback(java.lang.foreign.MemorySegment seg) {
            String str = seg.reinterpret(Long.MAX_VALUE).getString(0);
            internal_onStringCallback(str);
        }
    */
    /*[-TEAVM_C;-ADD]
        private static final java.util.ArrayList<CallbackClassManual> TEAVMC_CALLBACKS = new java.util.ArrayList<>();
    */
    /*[-TEAVM_C;-ADD]
        private int teavmcCallbackId = -1;
    */
    /*[-TEAVM_C;-ADD]
        private int teavmcRegisterCallback() {
            if(teavmcCallbackId < 0) {
                teavmcCallbackId = TEAVMC_CALLBACKS.size();
                TEAVMC_CALLBACKS.add(this);
            }
            else {
                TEAVMC_CALLBACKS.set(teavmcCallbackId, this);
            }
            return teavmcCallbackId;
        }
    */
    /*[-TEAVM_C;-ADD]
        private static String teavmcCStringToString(org.teavm.interop.Address address) {
            if(address == null || address.toLong() == 0) {
                return null;
            }
            int length = 0;
            while(address.add(length).getByte() != 0) {
                length++;
            }
            char[] chars = new char[length];
            for(int i = 0; i < length; i++) {
                chars[i] = (char)(address.add(i).getByte() & 0xFF);
            }
            return new String(chars);
        }
    */
    /*[-TEAVM_C;-ADD]
        private static abstract class TEAVMC_onVoidCallback_Function extends org.teavm.interop.Function {
            public abstract void call(int callbackId, long refData, long pointerData);
        }
    */
    /*[-TEAVM_C;-ADD]
        @org.teavm.interop.Export(name = "teavmc_CallbackClassManual_onVoidCallback")
        private static void teavmc_onVoidCallback(int callbackId, long refData, long pointerData) {
            TEAVMC_CALLBACKS.get(callbackId).internal_onVoidCallback(refData, pointerData);
        }
    */
    /*[-TEAVM_C;-ADD]
        private static abstract class TEAVMC_onIntCallback_Function extends org.teavm.interop.Function {
            public abstract int call(int callbackId, int intValue01, int intValue02);
        }
    */
    /*[-TEAVM_C;-ADD]
        @org.teavm.interop.Export(name = "teavmc_CallbackClassManual_onIntCallback")
        private static int teavmc_onIntCallback(int callbackId, int intValue01, int intValue02) {
            return TEAVMC_CALLBACKS.get(callbackId).internal_onIntCallback(intValue01, intValue02);
        }
    */
    /*[-TEAVM_C;-ADD]
        private static abstract class TEAVMC_onFloatCallback_Function extends org.teavm.interop.Function {
            public abstract float call(int callbackId, float floatValue01, float floatValue02);
        }
    */
    /*[-TEAVM_C;-ADD]
        @org.teavm.interop.Export(name = "teavmc_CallbackClassManual_onFloatCallback")
        private static float teavmc_onFloatCallback(int callbackId, float floatValue01, float floatValue02) {
            return TEAVMC_CALLBACKS.get(callbackId).internal_onFloatCallback(floatValue01, floatValue02);
        }
    */
    /*[-TEAVM_C;-ADD]
        private static abstract class TEAVMC_onBoolCallback_Function extends org.teavm.interop.Function {
            public abstract boolean call(int callbackId, boolean boolValue01);
        }
    */
    /*[-TEAVM_C;-ADD]
        @org.teavm.interop.Export(name = "teavmc_CallbackClassManual_onBoolCallback")
        private static boolean teavmc_onBoolCallback(int callbackId, boolean boolValue01) {
            return TEAVMC_CALLBACKS.get(callbackId).internal_onBoolCallback(boolValue01);
        }
    */
    /*[-TEAVM_C;-ADD]
        private static abstract class TEAVMC_onStringCallback_Function extends org.teavm.interop.Function {
            public abstract void call(int callbackId, org.teavm.interop.Address strValue01);
        }
    */
    /*[-TEAVM_C;-ADD]
        @org.teavm.interop.Export(name = "teavmc_CallbackClassManual_onStringCallback")
        private static void teavmc_onStringCallback(int callbackId, org.teavm.interop.Address strValue01) {
            TEAVMC_CALLBACKS.get(callbackId).internal_onStringCallback(teavmcCStringToString(strValue01));
        }
    */

    /*[-FFM;-ADD]
        private static final class FFMHandles {
            private static final boolean USE_CRITICAL = java.lang.Boolean.getBoolean("jparser.ffm.critical");
            private static final java.lang.foreign.Linker.Option[] LINKER_OPTIONS = USE_CRITICAL ? new java.lang.foreign.Linker.Option[] { java.lang.foreign.Linker.Option.critical(true) } : new java.lang.foreign.Linker.Option[0];
            private static final java.lang.foreign.SymbolLookup LOOKUP = java.lang.foreign.SymbolLookup.loaderLookup();
            private static final java.lang.foreign.Linker LINKER = java.lang.foreign.Linker.nativeLinker();
            static final java.lang.invoke.MethodHandle create_addr = LINKER.downcallHandle(LOOKUP.find("jparser_com_github_xpenatan_jParser_example_testlib_CallbackClassManual_internal_1native_1create_1addr__").orElseThrow(), java.lang.foreign.FunctionDescriptor.of(java.lang.foreign.ValueLayout.JAVA_LONG), LINKER_OPTIONS);
            static final java.lang.invoke.MethodHandle getAndroidCode = LINKER.downcallHandle(LOOKUP.find("jparser_com_github_xpenatan_jParser_example_testlib_CallbackClassManual_internal_1getAndroidCode__").orElseThrow(), java.lang.foreign.FunctionDescriptor.of(java.lang.foreign.ValueLayout.JAVA_LONG), LINKER_OPTIONS);
            static final java.lang.invoke.MethodHandle setupCallbacks = LINKER.downcallHandle(LOOKUP.find("jparser_com_github_xpenatan_jParser_example_testlib_CallbackClassManual_internal_1native_1setupCallbacks__JJJJJJ").orElseThrow(), java.lang.foreign.FunctionDescriptor.ofVoid(java.lang.foreign.ValueLayout.JAVA_LONG, java.lang.foreign.ValueLayout.JAVA_LONG, java.lang.foreign.ValueLayout.JAVA_LONG, java.lang.foreign.ValueLayout.JAVA_LONG, java.lang.foreign.ValueLayout.JAVA_LONG, java.lang.foreign.ValueLayout.JAVA_LONG), LINKER_OPTIONS);

            static final java.lang.invoke.MethodType MT_VOID = java.lang.invoke.MethodType.methodType(void.class, long.class, long.class);
            static final java.lang.invoke.MethodType MT_INT = java.lang.invoke.MethodType.methodType(int.class, int.class, int.class);
            static final java.lang.invoke.MethodType MT_FLOAT = java.lang.invoke.MethodType.methodType(float.class, float.class, float.class);
            static final java.lang.invoke.MethodType MT_BOOL = java.lang.invoke.MethodType.methodType(boolean.class, boolean.class);
            static final java.lang.invoke.MethodType MT_STRING = java.lang.invoke.MethodType.methodType(void.class, java.lang.foreign.MemorySegment.class);

            static final java.lang.foreign.FunctionDescriptor FD_VOID = java.lang.foreign.FunctionDescriptor.ofVoid(java.lang.foreign.ValueLayout.JAVA_LONG, java.lang.foreign.ValueLayout.JAVA_LONG);
            static final java.lang.foreign.FunctionDescriptor FD_INT = java.lang.foreign.FunctionDescriptor.of(java.lang.foreign.ValueLayout.JAVA_INT, java.lang.foreign.ValueLayout.JAVA_INT, java.lang.foreign.ValueLayout.JAVA_INT);
            static final java.lang.foreign.FunctionDescriptor FD_FLOAT = java.lang.foreign.FunctionDescriptor.of(java.lang.foreign.ValueLayout.JAVA_FLOAT, java.lang.foreign.ValueLayout.JAVA_FLOAT, java.lang.foreign.ValueLayout.JAVA_FLOAT);
            static final java.lang.foreign.FunctionDescriptor FD_BOOL = java.lang.foreign.FunctionDescriptor.of(java.lang.foreign.ValueLayout.JAVA_BOOLEAN, java.lang.foreign.ValueLayout.JAVA_BOOLEAN);
            static final java.lang.foreign.FunctionDescriptor FD_STRING = java.lang.foreign.FunctionDescriptor.ofVoid(java.lang.foreign.ValueLayout.ADDRESS);
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
    /*[-TEAVM_C;-NATIVE]
        long long myCode = 0;
        myCode++;
        #ifdef __ANDROID__
            return 1;
        #else
            return 0;
        #endif
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
    /*[-TEAVM_C;-NATIVE]
        return (int64_t)new CallbackClassManualImpl();
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
                ffm_releaseCallbacks();
                ffm_upcallArena = java.lang.foreign.Arena.ofShared();

                java.lang.invoke.MethodHandles.Lookup lookup = java.lang.invoke.MethodHandles.lookup();
                java.lang.foreign.Linker linker = FFMHandles.LINKER;

                java.lang.invoke.MethodHandle mh_void = lookup.findVirtual(CallbackClassManual.class, "internal_onVoidCallback", FFMHandles.MT_VOID).bindTo(this);
                ffm_stub_void = linker.upcallStub(mh_void, FFMHandles.FD_VOID, ffm_upcallArena);

                java.lang.invoke.MethodHandle mh_int = lookup.findVirtual(CallbackClassManual.class, "internal_onIntCallback", FFMHandles.MT_INT).bindTo(this);
                ffm_stub_int = linker.upcallStub(mh_int, FFMHandles.FD_INT, ffm_upcallArena);

                java.lang.invoke.MethodHandle mh_float = lookup.findVirtual(CallbackClassManual.class, "internal_onFloatCallback", FFMHandles.MT_FLOAT).bindTo(this);
                ffm_stub_float = linker.upcallStub(mh_float, FFMHandles.FD_FLOAT, ffm_upcallArena);

                java.lang.invoke.MethodHandle mh_bool = lookup.findVirtual(CallbackClassManual.class, "internal_onBoolCallback", FFMHandles.MT_BOOL).bindTo(this);
                ffm_stub_bool = linker.upcallStub(mh_bool, FFMHandles.FD_BOOL, ffm_upcallArena);

                java.lang.invoke.MethodHandle mh_string = lookup.findVirtual(CallbackClassManual.class, "internal_ffm_onStringCallback", FFMHandles.MT_STRING).bindTo(this);
                ffm_stub_string = linker.upcallStub(mh_string, FFMHandles.FD_STRING, ffm_upcallArena);

                internal_native_setupCallbacks(native_address, ffm_stub_void.address(), ffm_stub_int.address(), ffm_stub_float.address(), ffm_stub_bool.address(), ffm_stub_string.address());
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
                    internal_onStringCallback(gen.web.com.github.xpenatan.jparser.runtime.helper.NativeUtils.getJSString(strValue01));
                }
            };
            internal_native_setupCallbacks((int)native_address, onVoidCallback, onIntCallback, onFloatCallback, onBoolCallback, onStringCallback);
        }
    */
    /*[-TEAVM_C;-REPLACE_BLOCK]
        {
            int callbackId = teavmcRegisterCallback();
            internal_native_setupCallbacks(native_address, callbackId,
                    org.teavm.interop.Function.get(TEAVMC_onVoidCallback_Function.class, CallbackClassManual.class, "teavmc_onVoidCallback"),
                    org.teavm.interop.Function.get(TEAVMC_onIntCallback_Function.class, CallbackClassManual.class, "teavmc_onIntCallback"),
                    org.teavm.interop.Function.get(TEAVMC_onFloatCallback_Function.class, CallbackClassManual.class, "teavmc_onFloatCallback"),
                    org.teavm.interop.Function.get(TEAVMC_onBoolCallback_Function.class, CallbackClassManual.class, "teavmc_onBoolCallback"),
                    org.teavm.interop.Function.get(TEAVMC_onStringCallback_Function.class, CallbackClassManual.class, "teavmc_onStringCallback"));
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
    /*[-TEAVM_C;-NATIVE]
        // Replaced by the explicit TeaVM C import below.
    */
    /*[-TEAVM_C;-REPLACE]
        @org.teavm.interop.Import(name = "teavmc_CallbackClassManual_setupCallbacks")
        private static native void internal_native_setupCallbacks(long this_addr, int callbackId, TEAVMC_onVoidCallback_Function onVoidCallback_fp, TEAVMC_onIntCallback_Function onIntCallback_fp, TEAVMC_onFloatCallback_Function onFloatCallback_fp, TEAVMC_onBoolCallback_Function onBoolCallback_fp, TEAVMC_onStringCallback_Function onStringCallback_fp);
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
