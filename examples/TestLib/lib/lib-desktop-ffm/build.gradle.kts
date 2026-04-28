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
    api(project(":idl:api:api-core"))
    api(project(":idl:runtime:runtime-desktop-ffm"))
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

tasks.named("test") {
    dependsOn(
        ":examples:TestLib:app:desktop-ffm:test"
    )
}

project(":examples:TestLib:app:desktop-ffm").tasks.named("test") {
    outputs.upToDateWhen { false }
}

tasks.named("test") {
    outputs.upToDateWhen { false }
}

tasks.named("clean") {
    doFirst {
        val srcPath = "$projectDir/src/main/"
        project.delete(files(srcPath))
    }
}
