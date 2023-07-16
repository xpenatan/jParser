plugins {
    id("java")
}

dependencies {
    testImplementation(project(":jParser:loader"))
    testImplementation(project(":example:example-core"))
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
            ":example:example-build:clean",
            ":example:example-core:clean",
            ":example:example-build:generateNativeProject",
            "compileTestJava"
    )
}

tasks.named("test") {
    dependsOn(tasksOrder)
    mustRunAfter(tasksOrder)
}