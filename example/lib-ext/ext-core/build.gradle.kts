plugins {
    id("java")
}

dependencies {
    implementation(project(":example:lib:lib-core"))
}

tasks.named("clean") {
    doFirst {
        val srcPath = "$projectDir/src/main/"
        project.delete(files(srcPath))
    }
}