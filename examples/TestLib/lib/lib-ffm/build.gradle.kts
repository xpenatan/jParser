plugins {
    id("java")
    id("java-library")
}

java {
    sourceCompatibility = JavaVersion.toVersion(LibExt.javaFFMTarget)
    targetCompatibility = JavaVersion.toVersion(LibExt.javaFFMTarget)
}

dependencies {
    api(project(":jParser:loader:loader-core"))
    api(project(":jParser:api:api-core"))
    api(project(":jParser:runtime:runtime-jvm:ffm"))
}

// Bundle FFM-compiled native libraries into the JAR.
val libDir = "${projectDir}/../lib-build/build/c++/libs"
val windowsFile = "$libDir/windows/vc/ffm/TestLib64.dll"
val linuxFile = "$libDir/linux/ffm/libTestLib64.so"
val macFile = "$libDir/mac/ffm/libTestLib64.dylib"
val macArmFile = "$libDir/mac/arm/ffm/libTestLibarm64.dylib"
val hostNativeBuildTask = LibExt.hostBuildProjectTask(":examples:TestLib:lib:lib-build", "TestLib", "ffm")

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
