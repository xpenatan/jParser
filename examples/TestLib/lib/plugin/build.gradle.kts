import com.github.xpenatan.jParser.builder.tool.JParserSymbolNameMode
import com.github.xpenatan.jParser.gradle.JParserTargets

plugins {
    alias(libs.plugins.jParser)
}

val defaultNativeTargets = listOf(
    JParserTargets.WINDOWS64_JNI,
    JParserTargets.LINUX64_JNI,
    JParserTargets.MAC64_JNI,
    JParserTargets.MAC_ARM_JNI,
    JParserTargets.ANDROID_JNI,
    JParserTargets.IOS_JNI,
    JParserTargets.WINDOWS64_FFM,
    JParserTargets.LINUX64_FFM,
    JParserTargets.MAC64_FFM,
    JParserTargets.MAC_ARM_FFM,
    JParserTargets.LINUX64_TEAVM_C,
    JParserTargets.MAC64_TEAVM_C,
    JParserTargets.MAC_ARM_TEAVM_C,
    JParserTargets.ANDROID_TEAVM_C,
    JParserTargets.IOS_TEAVM_C
)

jParser {
    libName.set("TestLib")
    modulePrefix.set("")
    modulePath.set(file("..").absolutePath)
    moduleBaseSuffix.set("base")
    moduleBuildSuffix.set("builder")
    moduleCoreSuffix.set("core")
    moduleJNISuffix.set("shared/TestLib-jni")
    moduleFFMSuffix.set("desktop/TestLib-desktop-ffm")
    moduleWebSuffix.set("web/TestLib-web")
    moduleCSuffix.set("shared/TestLib-c")
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
        targetVariant(JParserTargets.WINDOWS64_TEAVM_C, "mt") {
            compileFlag("/MT")
        }
        targetVariant(JParserTargets.WINDOWS64_TEAVM_C, "md") {
            compileFlag("/MD")
        }
        target(JParserTargets.WEB_WASM) {
            linkerFlag("-Wl,--export-all")
            linkerFlag("-lc++abi")
            linkerFlag("-lc++")
            linkerFlag("-lc")
        }
    }
}
