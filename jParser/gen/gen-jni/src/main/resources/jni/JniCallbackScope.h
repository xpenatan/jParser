#include <jni.h>

// One scope per invocation: JNIEnv and local references belong to the calling thread.
class JniCallbackScope {
    JavaVM* vm;
    JNIEnv* env = nullptr;
    bool attached = false;
    bool localFrame = false;
    bool ready = false;

public:
    explicit JniCallbackScope(JavaVM* vm, int localCapacity = 0) : vm(vm) {
        if(vm == nullptr) return;
        jint status = vm->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION_1_6);
        if(status == JNI_EDETACHED) {
            // The invocation table has the same signature on Android and desktop JNI.
            status = vm->functions->AttachCurrentThread(vm, reinterpret_cast<void**>(&env), nullptr);
            attached = status == JNI_OK;
        }
        if(status != JNI_OK) {
            env = nullptr;
            return;
        }
        if(localCapacity > 0) {
            if(env->ExceptionCheck() || env->PushLocalFrame(localCapacity) != JNI_OK) return;
            localFrame = true;
        }
        ready = true;
    }

    ~JniCallbackScope() {
        if(localFrame) env->PopLocalFrame(nullptr);
        if(attached) {
            // A newly attached native thread has no Java caller to receive an exception.
            if(env->ExceptionCheck()) {
                env->ExceptionDescribe();
                env->ExceptionClear();
            }
            vm->DetachCurrentThread();
        }
    }

    JNIEnv* getEnv() const { return ready ? env : nullptr; }

    JniCallbackScope(const JniCallbackScope&) = delete;
    JniCallbackScope& operator=(const JniCallbackScope&) = delete;
};
