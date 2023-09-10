plugins {
    id("java")
}

val windowsFile = "$projectDir/../generator/build/c++/libs/exampleLib64.dll"

tasks.jar {
    from(windowsFile)
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
    ":example:lib:core:clean",
    ":example:lib:generator:clean",
    ":example:lib:generator:build_project",
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