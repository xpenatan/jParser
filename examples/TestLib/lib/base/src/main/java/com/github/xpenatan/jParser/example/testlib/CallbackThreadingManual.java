package com.github.xpenatan.jParser.example.testlib;

/** Native entry points for the desktop JNI threading regression tests. */
/*[-IDL_SKIP]*/
public class CallbackThreadingManual {
    /*[-JNI;-NATIVE]
        #include <thread>
    */

    /*[-JNI;-NATIVE]
        CallbackClass* callback = reinterpret_cast<CallbackClass*>(callbackAddress);
        int total = 0;
        for(int i = 0; i < iterations; ++i) {
            callback->onStringCallback("worker");
            if(env->ExceptionCheck()) return 0;
            total += callback->onIntCallback(3, 4);
            if(env->ExceptionCheck()) return 0;
        }
        return total;
    */
    public static native int invoke(long callbackAddress, int iterations);

    /*[-JNI;-NATIVE]
        JavaVM* vm = nullptr;
        env->GetJavaVM(&vm);
        CallbackClass* callback = reinterpret_cast<CallbackClass*>(callbackAddress);
        int total = 0;
        std::thread worker([&]() {
            JNIEnv* workerEnv = nullptr;
            if(preAttach && vm->functions->AttachCurrentThread(vm, reinterpret_cast<void**>(&workerEnv), nullptr) != JNI_OK) {
                total = -1;
                return;
            }
            for(int i = 0; i < iterations; ++i) {
                callback->onStringCallback("worker");
                total += callback->onIntCallback(3, 4);
                jint status = vm->GetEnv(reinterpret_cast<void**>(&workerEnv), JNI_VERSION_1_6);
                if(status != (preAttach ? JNI_OK : JNI_EDETACHED)) {
                    total = -2;
                    break;
                }
                if(preAttach && workerEnv->ExceptionCheck()) {
                    workerEnv->ExceptionClear();
                    total = -3;
                    break;
                }
            }
            if(preAttach) vm->DetachCurrentThread();
        });
        worker.join();
        return total;
    */
    public static native int invokeNativeWorker(long callbackAddress, int iterations, boolean preAttach);
}
