plugins {
    id("java")
    id("java-library")
}

java {
    sourceCompatibility = JavaVersion.toVersion(LibExt.java24Target)
    targetCompatibility = JavaVersion.toVersion(LibExt.java24Target)
}

dependencies {
    api(project(":loader:loader-core"))
    api(project(":idl:idl-core"))
    api(project(":idl-helper:idl-helper-desktop-ffm"))
}

// Bundle FFM-compiled native libraries into the JAR.
val libDir = "${projectDir}/../lib-build/build/c++/libs"
val windowsFile = "$libDir/windows/vc/ffm/TestLib64.dll"
val linuxFile = "$libDir/linux/ffm/libTestLib64.so"
val macFile = "$libDir/mac/ffm/libTestLib64.dylib"
val macArmFile = "$libDir/mac/arm/ffm/libTestLibarm64.dylib"

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
