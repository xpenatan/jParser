package com.github.xpenatan.jParser.gradle

/** Generation switches understood by the build runner for `jParser_generate`. */
enum class JParserGenerationTarget(val arg: String) {
    JNI("gen_jni"),
    FFM("gen_ffm"),
    WEB("gen_web"),
    TEAVM_C("gen_teavm_c")
}
