#include <jni.h>

extern int main(int argc, char** argv);

JNIEXPORT jboolean JNICALL Java_com_github_xpenatan_jParser_example_testlib_androidc_AndroidCBridge_runTestLibTest(JNIEnv* env, jclass clazz) {
    (void)env;
    (void)clazz;
    char* argv[] = { "TestLibTeaVMC", 0 };
    return main(1, argv) == 0 ? JNI_TRUE : JNI_FALSE;
}
