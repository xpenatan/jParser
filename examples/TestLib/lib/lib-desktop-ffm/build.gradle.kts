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

// Make the library's test task trigger the platform app tests that run the
// shared headless integration test. The lib project itself has no test
// sources, so running its `test` task should still execute the app tests
// to provide a single entry-point for CI scripts that invoke the lib test.
tasks.named("test") {
    dependsOn(
        ":examples:TestLib:app:desktop-ffm:test"
    )
}

tasks.named("clean") {
    doFirst {
        val srcPath = "$projectDir/src/main/"
        project.delete(files(srcPath))
    }
}
