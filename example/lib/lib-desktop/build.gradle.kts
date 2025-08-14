plugins {
    id("java")
}

java {
    sourceCompatibility = JavaVersion.toVersion(LibExt.java8Target)
    targetCompatibility = JavaVersion.toVersion(LibExt.java8Target)
}

val libDir = "${projectDir}/../lib-build/build/c++/libs"
//val windowsFile = "$libDir/windows/TestLib64.dll"
val windowsFile = "$libDir/windows/vc/TestLib64.dll"
val linuxFile = "$libDir/linux/libTestLib64.so"
val macFile = "$libDir/mac/libTestLib64.dylib"
val macArmFile = "$libDir/mac/arm/libTestLibarm64.dylib"

tasks.jar {
    from(windowsFile)
    from(linuxFile)
    from(macFile)
    from(macArmFile)
}

dependencies {
    if(LibExt.exampleUseRepoLibs) {
        testImplementation("com.github.xpenatan.jParser:loader-core:${LibExt.libVersion}")
    }
    else {
        testImplementation(project(":jParser:loader:loader-core"))
    }
    testImplementation(project(":example:lib:lib-core"))
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