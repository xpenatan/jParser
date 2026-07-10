package com.github.xpenatan.jParser.gradle

enum class JParserTargets(
    val targetName: String,
    internal val generationTarget: JParserGenerationTarget
) {
    WEB_WASM("web_wasm", JParserGenerationTarget.WEB),

    WINDOWS64_JNI("windows64_jni", JParserGenerationTarget.JNI),
    LINUX64_JNI("linux64_jni", JParserGenerationTarget.JNI),
    MAC64_JNI("mac64_jni", JParserGenerationTarget.JNI),
    MAC_ARM_JNI("macArm_jni", JParserGenerationTarget.JNI),
    ANDROID_JNI("android_jni", JParserGenerationTarget.JNI),
    IOS_JNI("ios_jni", JParserGenerationTarget.JNI),

    WINDOWS64_FFM("windows64_ffm", JParserGenerationTarget.FFM),
    LINUX64_FFM("linux64_ffm", JParserGenerationTarget.FFM),
    MAC64_FFM("mac64_ffm", JParserGenerationTarget.FFM),
    MAC_ARM_FFM("macArm_ffm", JParserGenerationTarget.FFM),

    WINDOWS64_TEAVM_C("windows64_teavm_c", JParserGenerationTarget.TEAVM_C),
    LINUX64_TEAVM_C("linux64_teavm_c", JParserGenerationTarget.TEAVM_C),
    MAC64_TEAVM_C("mac64_teavm_c", JParserGenerationTarget.TEAVM_C),
    MAC_ARM_TEAVM_C("macArm_teavm_c", JParserGenerationTarget.TEAVM_C),
    ANDROID_TEAVM_C("android_teavm_c", JParserGenerationTarget.TEAVM_C),
    IOS_TEAVM_C("ios_teavm_c", JParserGenerationTarget.TEAVM_C);

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
