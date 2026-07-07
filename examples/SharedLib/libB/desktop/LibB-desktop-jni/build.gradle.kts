plugins {
    id("java-library")
}

val moduleName = "LibB-desktop-jni"

val libDir = "${projectDir}/../../builder/build/c++/libs"
val windowsFile = "$libDir/windows/vc/jni/LibB64.dll"
val linuxFile = "$libDir/linux/jni/libLibB64.so"
val macFile = "$libDir/mac/jni/libLibB64.dylib"
val macArmFile = "$libDir/mac/arm/jni/libLibBarm64.dylib"

val platforms: Map<String, String> = mapOf(
    "windows_x64" to windowsFile,
    "linux_x64" to linuxFile,
    "mac_x64" to macFile,
    "mac_arm64" to macArmFile,
)

dependencies {
    implementation(project(":examples:SharedLib:libA:desktop:LibA-desktop-jni"))
    api(project(":examples:SharedLib:libB:shared:LibB-jni"))
}

java {
    sourceCompatibility = JavaVersion.toVersion(LibExt.javaMainTarget)
    targetCompatibility = JavaVersion.toVersion(LibExt.javaMainTarget)
}

platforms.forEach { (platform, nativeFile) ->
    tasks.register<Jar>("nativeJar_${platform}") {
        dependsOn(":examples:SharedLib:libB:builder:LibB_build_project")
        from(nativeFile)
        archiveBaseName.set("${moduleName}-${platform}")
        archiveClassifier.set("")
    }
}

tasks.named<Jar>("jar") {
    dependsOn(":examples:SharedLib:libB:builder:LibB_build_project")
    archiveBaseName.set(moduleName)
    archiveClassifier.set("")
    platforms.values.forEach { from(it) }
}
