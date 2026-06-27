plugins {
    id("base")
}

val moduleName = "LibA-c"

val libDir = "${projectDir}/../../lib-build/build/c++/libs"
val windowsFile = "$libDir/windows/vc/teavm_c/LibA64.dll"
val linuxFile = "$libDir/linux/teavm_c/libLibA64.so"
val macFile = "$libDir/mac/teavm_c/libLibA64.dylib"
val macArmFile = "$libDir/mac/arm/teavm_c/libLibAarm64.dylib"

val platforms: Map<String, String> = mapOf(
    "windows_x64" to windowsFile,
    "linux_x64" to linuxFile,
    "mac_x64" to macFile,
    "mac_arm64" to macArmFile,
)

platforms.forEach { (platform, nativeFile) ->
    tasks.register<Jar>("nativeJar_${platform}") {
        from(nativeFile)
        archiveBaseName.set("${moduleName}-${platform}")
        archiveClassifier.set("")
    }
}
