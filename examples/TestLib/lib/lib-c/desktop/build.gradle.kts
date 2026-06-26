plugins {
    id("base")
}

val moduleName = "TestLib-c"

val libDir = "${projectDir}/../../lib-build/build/c++/libs"
val windowsFile = "$libDir/windows/vc/teavm_c/TestLib64.dll"
val linuxFile = "$libDir/linux/teavm_c/libTestLib64.so"
val macFile = "$libDir/mac/teavm_c/libTestLib64.dylib"
val macArmFile = "$libDir/mac/arm/teavm_c/libTestLibarm64.dylib"

val nativeBuildTasks: Map<String, String> = mapOf(
    "windows_x64" to ":examples:TestLib:lib:plugin:jParser_build_windows64_teavm_c",
    "linux_x64" to ":examples:TestLib:lib:plugin:jParser_build_linux64_teavm_c",
    "mac_x64" to ":examples:TestLib:lib:plugin:jParser_build_mac64_teavm_c",
    "mac_arm64" to ":examples:TestLib:lib:plugin:jParser_build_macArm_teavm_c",
)

val platforms: Map<String, String> = mapOf(
    "windows_x64" to windowsFile,
    "linux_x64" to linuxFile,
    "mac_x64" to macFile,
    "mac_arm64" to macArmFile,
)

platforms.forEach { (platform, nativeFile) ->
    tasks.register<Jar>("nativeJar_${platform}") {
        dependsOn(nativeBuildTasks.getValue(platform))
        from(nativeFile)
        archiveBaseName.set("${moduleName}-${platform}")
        archiveClassifier.set("")
    }
}
