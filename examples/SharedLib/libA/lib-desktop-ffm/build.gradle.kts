plugins {
    id("java")
    id("java-library")
}

java {
    sourceCompatibility = JavaVersion.toVersion(LibExt.javaFFMTarget)
    targetCompatibility = JavaVersion.toVersion(LibExt.javaFFMTarget)
}

dependencies {
    api(project(":loader:loader-core"))
    api(project(":idl:idl-core"))
    api(project(":idl-helper:idl-helper-desktop-ffm"))
}

// Bundle FFM-compiled native libraries into the JAR.
val libDir = "${projectDir}/../lib-build/build/c++/libs"
val windowsFile = "$libDir/windows/vc/ffm/LibA64.dll"
val linuxFile = "$libDir/linux/ffm/libLibA64.so"
val macFile = "$libDir/mac/ffm/libLibA64.dylib"
val macArmFile = "$libDir/mac/arm/ffm/libLibAarm64.dylib"

tasks.jar {
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
