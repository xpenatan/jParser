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

tasks.jar {
    from(windowsFile)
    from(linuxFile)
    from(macFile)
    from(macArmFile)
}

tasks.named("test") {
    dependsOn(
        ":examples:TestLib:app:desktop-jni:test"
    )
}

dependencies {
    api(project(":loader:loader-core"))
    api(project(":idl:api:api-core"))
    api(project(":idl:runtime:runtime-desktop-jni"))

    testImplementation(project(":loader:loader-core"))
    testImplementation(project(":examples:TestLib:lib:lib-core"))
    testImplementation("junit:junit:${LibExt.jUnitVersion}")
}

tasks.named("clean") {
    doFirst {
        val srcPath = "$projectDir/src/main/"
        project.delete(files(srcPath))
    }
}