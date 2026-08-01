import com.github.xpenatan.jParser.builder.bundle.NativeBridge
import com.github.xpenatan.jParser.builder.bundle.NativeTarget

plugins {
    alias(libs.plugins.jParser)
    `maven-publish`
}

jParser {
    libName.set("TestLib")

    resources {
        license(rootProject.file("LICENSE"))

        declaredClassifier("windows-x86_64-jni")
        declaredClassifier("linux-x86_64-jni")
        declaredClassifier("macos-x86_64-jni")
        declaredClassifier("macos-arm64-jni")

        variant("windowsJni") {
            target.set(
                NativeTarget.of(
                    NativeTarget.OperatingSystem.WINDOWS,
                    NativeTarget.Architecture.X86_64
                )
            )
            bridge.set(NativeBridge.JNI)
            toolchainId.set("msvc")
            cRuntime.set("msvc-md")
            cppRuntime.set("msvc")
            implementationArchive(
                rootProject.file(
                    "examples/TestLib/lib/builder/build/c++/libs/windows/vc/jni/TestLib64_.lib"
                )
            )
            bridgeArchive(
                rootProject.file(
                    "examples/TestLib/lib/builder/build/c++/libs/windows/vc/jni/TestLib_bridge64_.lib"
                )
            )
            builtBy(
                ":examples:TestLib:lib:builder:TestLib_build_project_windows64_jni"
            )
        }

        variant("linuxJni") {
            target.set(
                NativeTarget.of(
                    NativeTarget.OperatingSystem.LINUX,
                    NativeTarget.Architecture.X86_64
                )
            )
            bridge.set(NativeBridge.JNI)
            toolchainId.set("gcc")
            cRuntime.set("glibc")
            cppRuntime.set("libstdc++")
            implementationArchive(
                rootProject.file(
                    "examples/TestLib/lib/builder/build/c++/libs/linux/jni/libTestLib64_.a"
                )
            )
            bridgeArchive(
                rootProject.file(
                    "examples/TestLib/lib/builder/build/c++/libs/linux/jni/libTestLib_bridge64_.a"
                )
            )
            builtBy(
                ":examples:TestLib:lib:builder:TestLib_build_project_linux64_jni"
            )
        }

        variant("macosX64Jni") {
            target.set(
                NativeTarget.of(
                    NativeTarget.OperatingSystem.MACOS,
                    NativeTarget.Architecture.X86_64
                )
            )
            bridge.set(NativeBridge.JNI)
            toolchainId.set("apple-clang")
            cRuntime.set("libsystem")
            cppRuntime.set("libc++")
            minimumPlatformVersion.set("10.13")
            implementationArchive(
                rootProject.file(
                    "examples/TestLib/lib/builder/build/c++/libs/mac/jni/libTestLib64_.a"
                )
            )
            bridgeArchive(
                rootProject.file(
                    "examples/TestLib/lib/builder/build/c++/libs/mac/jni/libTestLib_bridge64_.a"
                )
            )
            builtBy(
                ":examples:TestLib:lib:builder:TestLib_build_project_mac64_jni"
            )
        }

        variant("macosArm64Jni") {
            target.set(
                NativeTarget.of(
                    NativeTarget.OperatingSystem.MACOS,
                    NativeTarget.Architecture.ARM64
                )
            )
            bridge.set(NativeBridge.JNI)
            toolchainId.set("apple-clang")
            cRuntime.set("libsystem")
            cppRuntime.set("libc++")
            minimumPlatformVersion.set("10.13")
            implementationArchive(
                rootProject.file(
                    "examples/TestLib/lib/builder/build/c++/libs/mac/arm/jni/libTestLib64_.a"
                )
            )
            bridgeArchive(
                rootProject.file(
                    "examples/TestLib/lib/builder/build/c++/libs/mac/arm/jni/libTestLib_bridge64_.a"
                )
            )
            builtBy(
                ":examples:TestLib:lib:builder:TestLib_build_project_macArm_jni"
            )
        }
    }
}
