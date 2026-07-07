plugins {
    id("com.github.xpenatan.jparser")
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
        target("windows64_jni") {
            compileFlag("/DLIBA_EXPORTS")
            compileFlag(windowsUserConfig)
        }
        target("windows64_ffm") {
            compileFlag("/DLIBA_EXPORTS")
            compileFlag(windowsUserConfig)
        }
        target("windows64_teavm_c") {
            compileFlag("/DLIBA_EXPORTS")
            compileFlag(windowsUserConfig)
        }
        target("linux64_jni") {
            compileFlag(nativeUserConfig)
            compileFlag("-fvisibility=hidden")
            linkerFlag("-Wl,-soname,libLibA64.so")
        }
        target("linux64_ffm") {
            compileFlag(nativeUserConfig)
            compileFlag("-fvisibility=hidden")
            linkerFlag("-Wl,-soname,libLibA64.so")
        }
        target("linux64_teavm_c") {
            compileFlag(nativeUserConfig)
        }
        target("mac64_jni") {
            compileFlag(nativeUserConfig)
        }
        target("mac64_ffm") {
            compileFlag(nativeUserConfig)
        }
        target("mac64_teavm_c") {
            compileFlag(nativeUserConfig)
        }
        target("macArm_jni") {
            compileFlag(nativeUserConfig)
        }
        target("macArm_ffm") {
            compileFlag(nativeUserConfig)
        }
        target("macArm_teavm_c") {
            compileFlag(nativeUserConfig)
        }
        target("android_jni") {
            compileFlag(nativeUserConfig)
            linkerFlag("-Wl,-soname,libLibA.so")
        }
        target("android_teavm_c") {
            compileFlag("-DLIBA_EXPORTS")
            compileFlag(nativeUserConfig)
        }
    }
}
