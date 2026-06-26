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
val libBCustomHeader = file("../lib-build/src/main/cpp/custom/LibBCustomCode.h").absolutePath

jParser {
    libName.set("LibB")
    modulePrefix.set("lib")
    modulePath.set(file("..").absolutePath)
    packageName.set("libB")
    cppSourcePath.set("/src/main/cpp/source")
    webForcedInclude.set(libBCustomHeader)

    dependency("libA") {
        reference(
            libName = "LibA",
            packageName = "libA",
            modulePath = file("../../libA").absolutePath,
            projectPath = ":examples:SharedLib:libA:plugin"
        )

        native {
            target("windows64_jni") {
                compileFlag(windowsUserConfig)
            }
            target("windows64_ffm") {
                compileFlag(windowsUserConfig)
            }
            target("windows64_teavm_c") {
                compileFlag(windowsUserConfig)
            }
            target("linux64_jni") {
                compileFlag(nativeUserConfig)
                compileFlag("-fvisibility=hidden")
            }
            target("linux64_ffm") {
                compileFlag(nativeUserConfig)
                compileFlag("-fvisibility=hidden")
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
                linkerFlag("-Wl,--allow-shlib-undefined")
            }
            target("android_teavm_c") {
                compileFlag(nativeUserConfig)
                linkerFlag("-Wl,--allow-shlib-undefined")
            }
            target("web_wasm") {
                compileFlag(nativeUserConfig)
            }
        }
    }
}
