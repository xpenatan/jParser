        plugins {
    id("java")
    id("java-library")
}

java {
    sourceCompatibility = JavaVersion.toVersion(LibExt.javaMainTarget)
    targetCompatibility = JavaVersion.toVersion(LibExt.javaMainTarget)
}

dependencies {

    implementation(project(":examples:SharedLib:libA:core"))

    api(project(":jParser:loader:loader-core"))

    api(project(":jParser:api:api-core"))
    implementation(project(":jParser:runtime:core"))
}

tasks.named("compileJava") {
    dependsOn(":examples:SharedLib:libB:builder:LibB_build_project")
}

tasks.named("clean") {
    doFirst {
        val srcPath = "$projectDir/src/main/"
        project.delete(files(srcPath))
    }
}
