plugins {
    id("java-library")
}

val moduleName = "LibA-desktop-jni"

val libDir = "${projectDir}/../../builder/build/c++/libs"
val windowsFile = "$libDir/windows/vc/jni/LibA64.dll"
val linuxFile = "$libDir/linux/jni/libLibA64.so"
val macFile = "$libDir/mac/jni/libLibA64.dylib"
val macArmFile = "$libDir/mac/arm/jni/libLibAarm64.dylib"

val platforms: Map<String, String> = mapOf(
    "windows_x64" to windowsFile,
    "linux_x64" to linuxFile,
    "mac_x64" to macFile,
    "mac_arm64" to macArmFile,
)

dependencies {
    api(project(":examples:SharedLib:libA:shared:LibA-jni"))
}

java {
    sourceCompatibility = JavaVersion.toVersion(LibExt.javaMainTarget)
    targetCompatibility = JavaVersion.toVersion(LibExt.javaMainTarget)
}

platforms.forEach { (platform, nativeFile) ->
    tasks.register<Jar>("nativeJar_${platform}") {
        dependsOn(":examples:SharedLib:libA:builder:LibA_build_project")
        from(nativeFile)
        archiveBaseName.set("${moduleName}-${platform}")
        archiveClassifier.set("")
    }
}

tasks.named<Jar>("jar") {
    dependsOn(":examples:SharedLib:libA:builder:LibA_build_project")
    archiveBaseName.set(moduleName)
    archiveClassifier.set("")
    platforms.values.forEach { from(it) }
}
