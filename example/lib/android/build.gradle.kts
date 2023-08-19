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

val tasksOrder = tasks.register<GradleBuild>("prepareTest") {
    tasks = listOf(
            ":example:lib:generator:clean",
            ":example:lib:core:clean",
            ":example:lib:generator:generateNativeProject",
            "compileTestJava"
    )
}

tasks.named("test") {
    dependsOn(tasksOrder)
    mustRunAfter(tasksOrder)
}