plugins {
    id("java")
    id("java-library")
}

java {
    sourceCompatibility = JavaVersion.toVersion(LibExt.java8Target)
    targetCompatibility = JavaVersion.toVersion(LibExt.java8Target)
}

val libDir = "${projectDir}/../lib-build/build/c++/libs"
val windowsFile = "$libDir/windows/vc/jni/LibB64.dll"
val linuxFile = "$libDir/linux/jni/libLibB64.so"
val macFile = "$libDir/mac/jni/libLibB64.dylib"
val macArmFile = "$libDir/mac/arm/jni/libLibBarm64.dylib"

tasks.jar {
    from(windowsFile)
    from(linuxFile)
    from(macFile)
    from(macArmFile)
}

dependencies {
    implementation(project(":examples:SharedLib:libA:lib-desktop-jni"))
    api(project(":loader:loader-core"))
    api(project(":idl:idl-core"))
    api(project(":idl-helper:idl-helper-desktop-jni"))
}

tasks.named("clean") {
    doFirst {
        val srcPath = "$projectDir/src/main/"
        project.delete(files(srcPath))
    }
}