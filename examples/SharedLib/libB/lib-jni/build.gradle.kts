plugins {
    id("java")
    id("java-library")
}

java {
    sourceCompatibility = JavaVersion.toVersion(LibExt.javaMainTarget)
    targetCompatibility = JavaVersion.toVersion(LibExt.javaMainTarget)
}

val libDir = "${projectDir}/../lib-build/build/c++/libs"
val windowsFile = "$libDir/windows/vc/jni/LibB64.dll"
val linuxFile = "$libDir/linux/jni/libLibB64.so"
val macFile = "$libDir/mac/jni/libLibB64.dylib"
val macArmFile = "$libDir/mac/arm/jni/libLibBarm64.dylib"
val hostNativeBuildTask = LibExt.hostBuildProjectTask(":examples:SharedLib:libB:lib-build", "LibB", "jni")

tasks.jar {
    dependsOn(hostNativeBuildTask)
    from(windowsFile)
    from(linuxFile)
    from(macFile)
    from(macArmFile)
}

dependencies {
    implementation(project(":examples:SharedLib:libA:lib-jni"))
    api(project(":jParser:loader:loader-core"))
    api(project(":jParser:api:api-core"))
    api(project(":jParser:runtime:runtime-jvm:jni"))
}

tasks.named("compileJava") {
    dependsOn(":examples:SharedLib:libB:lib-build:LibB_build_project")
}

tasks.named("clean") {
    doFirst {
        val srcPath = "$projectDir/src/main/"
        project.delete(files(srcPath))
    }
}
