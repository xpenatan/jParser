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

tasks.jar {
    from(windowsFile)
    from(linuxFile)
    from(macFile)
    from(macArmFile)
}

dependencies {
    api(project(":loader:loader-core"))
    api(project(":idl:api:api-core"))
    api(project(":idl:runtime:runtime-desktop-jni"))
}

tasks.named("clean") {
    doFirst {
        val srcPath = "$projectDir/src/main/"
        project.delete(files(srcPath))
    }
}