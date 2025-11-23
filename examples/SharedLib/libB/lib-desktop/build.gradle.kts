plugins {
    id("java")
}

java {
    sourceCompatibility = JavaVersion.toVersion(LibExt.java8Target)
    targetCompatibility = JavaVersion.toVersion(LibExt.java8Target)
}

val libDir = "${projectDir}/../lib-build/build/c++/libs"
//val windowsFile = "$libDir/windows/LibB64.dll"
val windowsFile = "$libDir/windows/vc/LibB64.dll"
val linuxFile = "$libDir/linux/libLibB64.so"
val macFile = "$libDir/mac/libLibB64.dylib"
val macArmFile = "$libDir/mac/arm/libLibBarm64.dylib"

tasks.jar {
    from(windowsFile)
    from(linuxFile)
    from(macFile)
    from(macArmFile)
}

dependencies {
}

tasks.named("clean") {
    doFirst {
        val srcPath = "$projectDir/src/main/"
        project.delete(files(srcPath))
    }
}