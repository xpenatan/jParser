import com.github.xpenatan.jParser.builder.bundle.NativeBridge
import com.github.xpenatan.jParser.builder.bundle.NativeResourceClassifier
import com.github.xpenatan.jParser.builder.bundle.NativeTarget

plugins {
    alias(libs.plugins.jParser)
    `maven-publish`
}

val libraryName = "LibB"
val builderDirectory = "examples/SharedLib/libB/builder"
val builderProject = ":examples:SharedLib:libB:builder"

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

        fun desktopBridgeVariants(bridge: NativeBridge, suffix: String) {
            val bridgeId = bridge.id
            desktopVariant(
                "windows$suffix",
                NativeTarget.of(
                    NativeTarget.OperatingSystem.WINDOWS,
                    NativeTarget.Architecture.X86_64
                ),
                bridge,
                "msvc",
                "msvc-md",
                "msvc",
                "windows/vc/$bridgeId",
                "",
                "lib",
                "windows64_$bridgeId"
            )
            desktopVariant(
                "linux$suffix",
                NativeTarget.of(
                    NativeTarget.OperatingSystem.LINUX,
                    NativeTarget.Architecture.X86_64
                ),
                bridge,
                "gcc",
                "glibc",
                "libstdc++",
                "linux/$bridgeId",
                "lib",
                "a",
                "linux64_$bridgeId"
            )
            desktopVariant(
                "macosX64$suffix",
                NativeTarget.of(
                    NativeTarget.OperatingSystem.MACOS,
                    NativeTarget.Architecture.X86_64
                ),
                bridge,
                "apple-clang",
                "libsystem",
                "libc++",
                "mac/$bridgeId",
                "lib",
                "a",
                "mac64_$bridgeId",
                "10.13"
            )
            desktopVariant(
                "macosArm64$suffix",
                NativeTarget.of(
                    NativeTarget.OperatingSystem.MACOS,
                    NativeTarget.Architecture.ARM64
                ),
                bridge,
                "apple-clang",
                "libsystem",
                "libc++",
                "mac/arm/$bridgeId",
                "lib",
                "a",
                "macArm_$bridgeId",
                "10.13"
            )
        }

        desktopBridgeVariants(NativeBridge.JNI, "Jni")
        desktopBridgeVariants(NativeBridge.FFM, "Ffm")
    }
}
