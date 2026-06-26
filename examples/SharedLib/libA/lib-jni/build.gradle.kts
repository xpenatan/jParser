plugins {
    id("java")
    id("java-library")
}

java {
    sourceCompatibility = JavaVersion.toVersion(LibExt.javaMainTarget)
    targetCompatibility = JavaVersion.toVersion(LibExt.javaMainTarget)
}

val libDir = "${projectDir}/../lib-build/build/c++/libs"
val windowsFile = "$libDir/windows/vc/jni/LibA64.dll"
val linuxFile = "$libDir/linux/jni/libLibA64.so"
val macFile = "$libDir/mac/jni/libLibA64.dylib"
val macArmFile = "$libDir/mac/arm/jni/libLibAarm64.dylib"
val hostNativeBuildTask = LibExt.hostBuildProjectTask(":examples:SharedLib:libA:lib-build", "LibA", "jni")

tasks.jar {
    dependsOn(hostNativeBuildTask)
    from(windowsFile)
    from(linuxFile)
    from(macFile)
    from(macArmFile)
}

dependencies {
    api(project(":jParser:loader:loader-core"))
    api(project(":jParser:api:api-core"))
    api(project(":jParser:runtime:runtime-jvm:jni"))
}

tasks.named("compileJava") {
    dependsOn(":examples:SharedLib:libA:lib-build:LibA_build_project")
}

tasks.named("clean") {
    doFirst {
        val srcPath = "$projectDir/src/main/"
        project.delete(files(srcPath))
    }
}
