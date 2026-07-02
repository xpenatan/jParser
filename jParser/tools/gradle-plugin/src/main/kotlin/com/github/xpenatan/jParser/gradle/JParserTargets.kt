package com.github.xpenatan.jParser.gradle

object JParserTargets {
    const val WEB_WASM = "web_wasm"

    const val WINDOWS64_JNI = "windows64_jni"
    const val LINUX64_JNI = "linux64_jni"
    const val MAC64_JNI = "mac64_jni"
    const val MAC_ARM_JNI = "macArm_jni"
    const val ANDROID_JNI = "android_jni"
    const val IOS_JNI = "ios_jni"

    const val WINDOWS64_FFM = "windows64_ffm"
    const val LINUX64_FFM = "linux64_ffm"
    const val MAC64_FFM = "mac64_ffm"
    const val MAC_ARM_FFM = "macArm_ffm"

    const val WINDOWS64_TEAVM_C = "windows64_teavm_c"
    const val LINUX64_TEAVM_C = "linux64_teavm_c"
    const val MAC64_TEAVM_C = "mac64_teavm_c"
    const val MAC_ARM_TEAVM_C = "macArm_teavm_c"
    const val ANDROID_TEAVM_C = "android_teavm_c"
    const val IOS_TEAVM_C = "ios_teavm_c"

    val ALL = listOf(
        WEB_WASM,
        WINDOWS64_JNI,
        LINUX64_JNI,
        MAC64_JNI,
        MAC_ARM_JNI,
        ANDROID_JNI,
        IOS_JNI,
        WINDOWS64_FFM,
        LINUX64_FFM,
        MAC64_FFM,
        MAC_ARM_FFM,
        WINDOWS64_TEAVM_C,
        LINUX64_TEAVM_C,
        MAC64_TEAVM_C,
        MAC_ARM_TEAVM_C,
        ANDROID_TEAVM_C,
        IOS_TEAVM_C
    )
}
