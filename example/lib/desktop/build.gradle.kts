plugins {
    id("java")
}

val windowsFile = "$projectDir/../generator/build/c++/libs/windows/exampleLib64.dll"
val linuxFile = "$projectDir/../generator/build/c++/libs/linux/libexampleLib64.so"
val macFile = "$projectDir/../generator/build/c++/libs/mac/libexampleLib64.dylib"

tasks.jar {
    from(windowsFile)
    from(linuxFile)
    from(macFile)
}

dependencies {
    if(LibExt.exampleUseRepoLibs) {
        testImplementation("com.github.xpenatan.jParser:loader-core:${LibExt.libVersion}")
    }
    else {
        testImplementation(project(":jParser:loader:loader-core"))
    }
    testImplementation(project(":example:lib:core"))
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