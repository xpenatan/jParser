plugins {
    id("java")
}

java {
    sourceCompatibility = JavaVersion.toVersion(LibExt.java8Target)
    targetCompatibility = JavaVersion.toVersion(LibExt.java8Target)
}

val libDir = "${projectDir}/../lib-build/build/c++/libs"
//val windowsFile = "$libDir/windows/LibA64.dll"
val windowsFile = "$libDir/windows/vc/LibA64.dll"
val linuxFile = "$libDir/linux/libLibA64.so"
val macFile = "$libDir/mac/libLibA64.dylib"
val macArmFile = "$libDir/mac/arm/libLibAarm64.dylib"

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