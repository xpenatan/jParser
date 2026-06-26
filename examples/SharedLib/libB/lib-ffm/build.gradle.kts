plugins {
    id("java")
    id("java-library")
}

java {
    sourceCompatibility = JavaVersion.toVersion(LibExt.javaFFMTarget)
    targetCompatibility = JavaVersion.toVersion(LibExt.javaFFMTarget)
}

dependencies {
    implementation(project(":examples:SharedLib:libA:lib-ffm"))
    api(project(":jParser:loader:loader-core"))
    api(project(":jParser:api:api-core"))
    api(project(":jParser:runtime:runtime-jvm:ffm"))
}

// Bundle FFM-compiled native libraries into the JAR.
val libDir = "${projectDir}/../lib-build/build/c++/libs"
val windowsFile = "$libDir/windows/vc/ffm/LibB64.dll"
val linuxFile = "$libDir/linux/ffm/libLibB64.so"
val macFile = "$libDir/mac/ffm/libLibB64.dylib"
val macArmFile = "$libDir/mac/arm/ffm/libLibBarm64.dylib"
val hostNativeBuildTask = LibExt.hostBuildProjectTask(":examples:SharedLib:libB:lib-build", "LibB", "ffm")

tasks.named("compileJava") {
    dependsOn(":examples:SharedLib:libB:lib-build:LibB_build_project")
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
