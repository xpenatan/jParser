plugins {
    id("java-library")
}

val moduleName = "TestLib-desktop-jni"

val libDir = "${projectDir}/../../builder/build/c++/libs"
val windowsFile = "$libDir/windows/vc/jni/TestLib64.dll"
val linuxFile = "$libDir/linux/jni/libTestLib64.so"
val macFile = "$libDir/mac/jni/libTestLib64.dylib"
val macArmFile = "$libDir/mac/arm/jni/libTestLibarm64.dylib"

val platforms: Map<String, String> = mapOf(
    "windows_x64" to windowsFile,
    "linux_x64" to linuxFile,
    "mac_x64" to macFile,
    "mac_arm64" to macArmFile,
)

dependencies {
    api(project(":examples:TestLib:lib:shared:TestLib-jni"))
}

java {
    sourceCompatibility = JavaVersion.toVersion(libs.versions.javaMain.get())
    targetCompatibility = JavaVersion.toVersion(libs.versions.javaMain.get())
}

platforms.forEach { (platform, nativeFile) ->
    tasks.register<Jar>("nativeJar_${platform}") {
        dependsOn(":examples:TestLib:lib:builder:TestLib_build_project")
        from(nativeFile)
        archiveBaseName.set("${moduleName}-${platform}")
        archiveClassifier.set("")
    }
}

tasks.named<Jar>("jar") {
    dependsOn(":examples:TestLib:lib:builder:TestLib_build_project")
    archiveBaseName.set(moduleName)
    archiveClassifier.set("")
    platforms.values.forEach { from(it) }
}
