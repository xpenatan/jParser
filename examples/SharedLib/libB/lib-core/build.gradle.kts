        plugins {
    id("java")
    id("java-library")
}

java {
    sourceCompatibility = JavaVersion.toVersion(LibExt.javaMainTarget)
    targetCompatibility = JavaVersion.toVersion(LibExt.javaMainTarget)
}

dependencies {

    implementation(project(":examples:SharedLib:libA:lib-core"))

    api(project(":loader:loader-core"))

    api(project(":idl:api:api-core"))
    implementation(project(":idl:runtime:runtime-core"))
}

tasks.named("clean") {
    doFirst {
        val srcPath = "$projectDir/src/main/"
        project.delete(files(srcPath))
    }
}