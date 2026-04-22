plugins {
    id("java")
    id("java-library")
}

java {
    sourceCompatibility = JavaVersion.toVersion(LibExt.java8Target)
    targetCompatibility = JavaVersion.toVersion(LibExt.java8Target)
}

val libDir = "${projectDir}/../lib-build/build/c++/libs"
//val windowsFile = "$libDir/windows/TestLib64.dll"
val windowsFile = "$libDir/windows/vc/jni/TestLib64.dll"
val linuxFile = "$libDir/linux/jni/libTestLib64.so"
val macFile = "$libDir/mac/jni/libTestLib64.dylib"
val macArmFile = "$libDir/mac/arm/jni/libTestLibarm64.dylib"

tasks.jar {
    from(windowsFile)
    from(linuxFile)
    from(macFile)
    from(macArmFile)
}

dependencies {
    api(project(":loader:loader-core"))
    api(project(":idl:idl-core"))
    api(project(":idl-helper:idl-helper-desktop-jni"))

    testImplementation(project(":loader:loader-core"))
    testImplementation(project(":examples:TestLib:lib:lib-core"))
    testImplementation("junit:junit:${LibExt.jUnitVersion}")
}

tasks.named("clean") {
    doFirst {
        val srcPath = "$projectDir/src/main/"
        project.delete(files(srcPath))
    }
}

val testTasks = listOf(
    ":example:lib:core:build",
    "jar",
    "compileTestJava"
)

val tasksOrder = tasks.register<GradleBuild>("prepareTest") {
    tasks = testTasks
}

tasks.named("test") {
    dependsOn(tasksOrder)
    mustRunAfter(tasksOrder)
}