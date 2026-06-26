import com.github.xpenatan.jParser.builder.tool.JParserSymbolNameMode

plugins {
    id("com.github.xpenatan.jparser")
}

val defaultNativeTargets = listOf(
    "windows64_jni",
    "linux64_jni",
    "mac64_jni",
    "macArm_jni",
    "android_jni",
    "ios_jni",
    "windows64_ffm",
    "linux64_ffm",
    "mac64_ffm",
    "macArm_ffm",
    "windows64_teavm_c",
    "linux64_teavm_c",
    "mac64_teavm_c",
    "macArm_teavm_c",
    "android_teavm_c",
    "ios_teavm_c"
)

jParser {
    libName.set("TestLib")
    modulePrefix.set("lib")
    modulePath.set(file("..").absolutePath)
    packageName.set("com.github.xpenatan.jParser.example.testlib")
    cppSourcePath.set("/src/main/cpp/source/TestLib/src")

    jniSymbolNameMode.set(JParserSymbolNameMode.OBFUSCATED)
    ffmSymbolNameMode.set(JParserSymbolNameMode.OBFUSCATED)
    teaVMCSymbolNameMode.set(JParserSymbolNameMode.OBFUSCATED)
    ffmLogMethod.set(true)
    windowsDebugBuild.set(true)

    native {
        defaultNativeTargets.forEach { targetName ->
            target(targetName) {}
        }
        target("web_wasm") {
            linkerFlag("-Wl,--export-all")
            linkerFlag("-lc++abi")
            linkerFlag("-lc++")
            linkerFlag("-lc")
        }
    }
}
