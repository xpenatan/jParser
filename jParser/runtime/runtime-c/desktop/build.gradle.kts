plugins {
    id("base")
}

val moduleName = "runtime-c"

val libDir = "${projectDir}/../../runtime-build/build/c++/libs"
val windowsFile = "$libDir/windows/vc/teavm_c/runtime64.dll"
val linuxFile = "$libDir/linux/teavm_c/libruntime64.so"
val macFile = "$libDir/mac/teavm_c/libruntime64.dylib"
val macArmFile = "$libDir/mac/arm/teavm_c/libruntimearm64.dylib"

val nativeBuildTasks: Map<String, String> = mapOf(
    "windows_x64" to ":jParser:runtime:runtime-build:runtime_helper_build_project_windows64_teavm_c",
    "linux_x64" to ":jParser:runtime:runtime-build:runtime_helper_build_project_linux64_teavm_c",
    "mac_x64" to ":jParser:runtime:runtime-build:runtime_helper_build_project_mac64_teavm_c",
    "mac_arm64" to ":jParser:runtime:runtime-build:runtime_helper_build_project_macArm_teavm_c",
)

val platforms: Map<String, String> = mapOf(
    "windows_x64" to windowsFile,
    "linux_x64" to linuxFile,
    "mac_x64" to macFile,
    "mac_arm64" to macArmFile,
)

val nativeJars = platforms.map { (platform, nativeFile) ->
    platform to tasks.register<Jar>("nativeJar_${platform}") {
        dependsOn(nativeBuildTasks.getValue(platform))
        from(nativeFile)
        archiveBaseName.set("${moduleName}-${platform}")
        archiveClassifier.set("")
    }
}

val nativeRuntime by configurations.creating {
    isCanBeConsumed = true
    isCanBeResolved = false
}

artifacts {
    nativeJars.forEach { add(nativeRuntime.name, it.second) }
}

publishing {
    publications {
        nativeJars.forEach { (platform, nativeJar) ->
            create<MavenPublication>("mavenNative_${platform}") {
                artifactId = "${moduleName}_${platform}"
                group = LibExt.groupId
                version = LibExt.libVersion
                artifact(nativeJar)
            }
        }
    }
}
