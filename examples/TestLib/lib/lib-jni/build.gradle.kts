plugins {
    id("java")
    id("java-library")
}

java {
    sourceCompatibility = JavaVersion.toVersion(LibExt.javaMainTarget)
    targetCompatibility = JavaVersion.toVersion(LibExt.javaMainTarget)
}

val libDir = "${projectDir}/../lib-build/build/c++/libs"
//val windowsFile = "$libDir/windows/TestLib64.dll"
val windowsFile = "$libDir/windows/vc/jni/TestLib64.dll"
val linuxFile = "$libDir/linux/jni/libTestLib64.so"
val macFile = "$libDir/mac/jni/libTestLib64.dylib"
val macArmFile = "$libDir/mac/arm/jni/libTestLibarm64.dylib"
val hostNativeBuildTask = LibExt.hostBuildProjectTask(":examples:TestLib:lib:lib-build", "TestLib", "jni")

dependencies {
    api(project(":jParser:loader:loader-core"))
    api(project(":jParser:api:api-core"))
    api(project(":jParser:runtime:runtime-jvm:jni"))
}

tasks.named("compileJava") {
    dependsOn(":examples:TestLib:lib:lib-build:TestLib_build_project")
}

tasks.jar {
    dependsOn(hostNativeBuildTask)
    from(windowsFile)
    from(linuxFile)
    from(macFile)
    from(macArmFile)
}

tasks.named("clean") {
    doFirst {
        val srcPath = "$projectDir/src/main/"
        project.delete(files(srcPath))
    }
}
