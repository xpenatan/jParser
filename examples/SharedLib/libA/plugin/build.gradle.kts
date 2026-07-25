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
val customHeader = file("../builder/src/main/cpp/custom/LibACustomCode.h").absolutePath

jParser {
    libName.set("LibA")
    modulePrefix.set("")
    modulePath.set(file("..").absolutePath)
    moduleBaseSuffix.set("base")
    moduleBuildSuffix.set("builder")
    moduleCoreSuffix.set("core")
    moduleJNISuffix.set("shared/LibA-jni")
    moduleFFMSuffix.set("desktop/LibA-desktop-ffm")
    moduleWebSuffix.set("web/LibA-web")
    moduleCSuffix.set("shared/LibA-c")
    packageName.set("libA")
    cppSourcePath.set("/src/main/cpp/source")
    windowsDebugBuild.set(true)
    webSideModule.set(1)
    webForcedInclude.set(customHeader)

    native {
        target(JParserTargets.WINDOWS64_JNI) {
            compileFlag("/DLIBA_EXPORTS")
            compileFlag(windowsUserConfig)
        }
        target(JParserTargets.WINDOWS64_FFM) {
            compileFlag("/DLIBA_EXPORTS")
            compileFlag(windowsUserConfig)
        }
        target(JParserTargets.WINDOWS64_TEAVM_C) {
            compileFlag("/DLIBA_EXPORTS")
            compileFlag(windowsUserConfig)
        }
        target(JParserTargets.LINUX64_JNI) {
            compileFlag(nativeUserConfig)
            compileFlag("-fvisibility=hidden")
            linkerFlag("-Wl,-soname,libLibA64.so")
        }
        target(JParserTargets.LINUX64_FFM) {
            compileFlag(nativeUserConfig)
            compileFlag("-fvisibility=hidden")
            linkerFlag("-Wl,-soname,libLibA64.so")
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
            linkerFlag("-Wl,-soname,libLibA.so")
        }
        target(JParserTargets.ANDROID_TEAVM_C) {
            compileFlag("-DLIBA_EXPORTS")
            compileFlag(nativeUserConfig)
        }
        target(JParserTargets.IOS_TEAVM_C) {
            compileFlag("-DLIBA_EXPORTS")
            compileFlag(nativeUserConfig)
        }
    }
}
