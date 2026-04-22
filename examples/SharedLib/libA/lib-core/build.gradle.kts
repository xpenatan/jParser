        plugins {
    id("java")
    id("java-library")
}

java {
    sourceCompatibility = JavaVersion.toVersion(LibExt.java8Target)
    targetCompatibility = JavaVersion.toVersion(LibExt.java8Target)
}

dependencies {
    api(project(":loader:loader-core"))
    api(project(":idl:idl-core"))
    api(project(":idl-helper:idl-helper-core"))
}

tasks.named("clean") {
    doFirst {
        val srcPath = "$projectDir/src/main/"
        project.delete(files(srcPath))
    }
}