import com.github.xpenatan.jParser.builder.bundle.NativeBridge
import com.github.xpenatan.jParser.builder.bundle.NativeResourceClassifier
import com.github.xpenatan.jParser.builder.bundle.NativeTarget

plugins {
    alias(libs.plugins.jParser)
    `maven-publish`
}

val libraryName = "LibA"
val builderDirectory = "examples/SharedLib/libA/builder"
val builderProject = ":examples:SharedLib:libA:builder"

jParser {
    libName.set(libraryName)

    resources {
        license(rootProject.file("LICENSE"))

        fun desktopVariant(
            name: String,
            targetValue: NativeTarget,
            bridgeValue: NativeBridge,
            toolchain: String,
            cRuntimeValue: String,
            cppRuntimeValue: String,
            archiveDirectory: String,
            archivePrefix: String,
            archiveExtension: String,
            taskSuffix: String,
            minimumPlatform: String = ""
        ) {
            declaredClassifier(
                NativeResourceClassifier.of(targetValue, bridgeValue, "")
            )
            variant(name) {
                target.set(targetValue)
                bridge.set(bridgeValue)
                toolchainId.set(toolchain)
                cRuntime.set(cRuntimeValue)
                cppRuntime.set(cppRuntimeValue)
                if(minimumPlatform.isNotEmpty()) {
                    minimumPlatformVersion.set(minimumPlatform)
                }
                implementationArchive(
                    rootProject.file(
                        "$builderDirectory/build/c++/libs/$archiveDirectory/" +
                            "$archivePrefix$libraryName" + "64_.$archiveExtension"
                    )
                )
                bridgeArchive(
                    rootProject.file(
                        "$builderDirectory/build/c++/libs/$archiveDirectory/" +
                            "$archivePrefix${libraryName}_bridge64_.$archiveExtension"
                    )
                )
                builtBy("$builderProject:${libraryName}_build_project_$taskSuffix")
            }
        }

        desktopVariant(
            "windowsJni",
            NativeTarget.of(
                NativeTarget.OperatingSystem.WINDOWS,
                NativeTarget.Architecture.X86_64
            ),
            NativeBridge.JNI,
            "msvc",
            "msvc-md",
            "msvc",
            "windows/vc/jni",
            "",
            "lib",
            "windows64_jni"
        )
        desktopVariant(
            "linuxJni",
            NativeTarget.of(
                NativeTarget.OperatingSystem.LINUX,
                NativeTarget.Architecture.X86_64
            ),
            NativeBridge.JNI,
            "gcc",
            "glibc",
            "libstdc++",
            "linux/jni",
            "lib",
            "a",
            "linux64_jni"
        )
        desktopVariant(
            "macosX64Jni",
            NativeTarget.of(
                NativeTarget.OperatingSystem.MACOS,
                NativeTarget.Architecture.X86_64
            ),
            NativeBridge.JNI,
            "apple-clang",
            "libsystem",
            "libc++",
            "mac/jni",
            "lib",
            "a",
            "mac64_jni",
            "10.13"
        )
        desktopVariant(
            "macosArm64Jni",
            NativeTarget.of(
                NativeTarget.OperatingSystem.MACOS,
                NativeTarget.Architecture.ARM64
            ),
            NativeBridge.JNI,
            "apple-clang",
            "libsystem",
            "libc++",
            "mac/arm/jni",
            "lib",
            "a",
            "macArm_jni",
            "10.13"
        )
    }
}
