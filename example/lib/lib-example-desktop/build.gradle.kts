plugins {
    id("java")
}

dependencies {
    if(LibExt.exampleUseRepoLibs) {
        testImplementation("com.github.xpenatan.jParser:jParser-loader:${LibExt.libVersion}")
    }
    else {
        testImplementation(project(":jParser:loader"))
    }
    testImplementation(project(":example:lib:lib-example-core"))
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
            ":example:lib:lib-example-build:clean",
            ":example:lib:lib-example-core:clean",
            ":example:lib:lib-example-build:generateNativeProject",
            "compileTestJava"
    )
}

tasks.named("test") {
    dependsOn(tasksOrder)
    mustRunAfter(tasksOrder)
}