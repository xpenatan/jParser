package com.github.xpenatan.jParser.gradle

enum class JParserTargets(val targetName: String) {
    WEB_WASM("web_wasm"),

    WINDOWS64_JNI("windows64_jni"),
    LINUX64_JNI("linux64_jni"),
    MAC64_JNI("mac64_jni"),
    MAC_ARM_JNI("macArm_jni"),
    ANDROID_JNI("android_jni"),
    IOS_JNI("ios_jni"),

    WINDOWS64_FFM("windows64_ffm"),
    LINUX64_FFM("linux64_ffm"),
    MAC64_FFM("mac64_ffm"),
    MAC_ARM_FFM("macArm_ffm"),

    WINDOWS64_TEAVM_C("windows64_teavm_c"),
    LINUX64_TEAVM_C("linux64_teavm_c"),
    MAC64_TEAVM_C("mac64_teavm_c"),
    MAC_ARM_TEAVM_C("macArm_teavm_c"),
    ANDROID_TEAVM_C("android_teavm_c"),
    IOS_TEAVM_C("ios_teavm_c");

    override fun toString(): String = targetName

    companion object {
        @JvmField
        val ALL: List<JParserTargets> = values().toList()

        @JvmStatic
        fun fromTargetName(targetName: String): JParserTargets? {
            return values().firstOrNull { it.targetName == targetName }
        }
    }
}
