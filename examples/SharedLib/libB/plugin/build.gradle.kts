import com.github.xpenatan.jParser.gradle.JParserTargets

plugins {
    alias(libs.plugins.jParser)
}

val isWindowsHost = System.getProperty("os.name").startsWith("Windows", ignoreCase = true)
val nativeUserConfig = if(isWindowsHost) {
    "-DLIB_USER_CONFIG=\"\\\"LibACustomConfig.h\\\"\""
}
else {
    "-DLIB_USER_CONFIG=\"LibACustomConfig.h\""
}
val windowsUserConfig = "/DLIB_USER_CONFIG=\"\\\"LibACustomConfig.h\\\"\""
val libBCustomHeader = file("../builder/src/main/cpp/custom/LibBCustomCode.h").absolutePath

jParser {
    libName.set("LibB")
    modulePrefix.set("")
    modulePath.set(file("..").absolutePath)
    moduleBaseSuffix.set("base")
    moduleBuildSuffix.set("builder")
    moduleCoreSuffix.set("core")
    moduleJNISuffix.set("shared/LibB-jni")
    moduleFFMSuffix.set("desktop/LibB-desktop-ffm")
    moduleWebSuffix.set("web/LibB-web")
    moduleCSuffix.set("shared/LibB-c")
    packageName.set("libB")
    cppSourcePath.set("/src/main/cpp/source")
    webForcedInclude.set(libBCustomHeader)

    dependency("libA") {
        reference(
            libName = "LibA",
            packageName = "libA",
            modulePath = file("../../libA").absolutePath,
            modulePrefix = "",
            moduleBuildSuffix = "builder",
            projectPath = ":examples:SharedLib:libA:plugin"
        )

        native {
            target(JParserTargets.WINDOWS64_JNI) {
                compileFlag(windowsUserConfig)
            }
            target(JParserTargets.WINDOWS64_FFM) {
                compileFlag(windowsUserConfig)
            }
            target(JParserTargets.WINDOWS64_TEAVM_C) {
                compileFlag(windowsUserConfig)
            }
            target(JParserTargets.LINUX64_JNI) {
                compileFlag(nativeUserConfig)
                compileFlag("-fvisibility=hidden")
            }
            target(JParserTargets.LINUX64_FFM) {
                compileFlag(nativeUserConfig)
                compileFlag("-fvisibility=hidden")
            }
            target(JParserTargets.LINUX64_TEAVM_C) {
                compileFlag(nativeUserConfig)
            }
            target(JParserTargets.MAC64_JNI) {
                compileFlag(nativeUserConfig)
            }
            target(JParserTargets.MAC64_FFM) {
                compileFlag(nativeUserConfig)
            }
            target(JParserTargets.MAC64_TEAVM_C) {
                compileFlag(nativeUserConfig)
            }
            target(JParserTargets.MAC_ARM_JNI) {
                compileFlag(nativeUserConfig)
            }
            target(JParserTargets.MAC_ARM_FFM) {
                compileFlag(nativeUserConfig)
            }
            target(JParserTargets.MAC_ARM_TEAVM_C) {
                compileFlag(nativeUserConfig)
            }
            target(JParserTargets.ANDROID_JNI) {
                compileFlag(nativeUserConfig)
                linkerFlag("-Wl,--allow-shlib-undefined")
            }
            target(JParserTargets.ANDROID_TEAVM_C) {
                compileFlag(nativeUserConfig)
                linkerFlag("-Wl,--allow-shlib-undefined")
            }
            target(JParserTargets.IOS_TEAVM_C) {
                compileFlag(nativeUserConfig)
            }
            target(JParserTargets.WEB_WASM) {
                compileFlag(nativeUserConfig)
            }
        }
    }
}
