        plugins {
    id("java")
    id("java-library")
}

java {
    sourceCompatibility = JavaVersion.toVersion(LibExt.java8Target)
    targetCompatibility = JavaVersion.toVersion(LibExt.java8Target)
}

dependencies {

    implementation(project(":examples:SharedLib:libA:lib-core"))

    api(project(":loader:loader-core"))
    api(project(":idl:idl-core"))

    implementation(project(":idl-helper:idl-helper-core"))
}

tasks.named("clean") {
    doFirst {
        val srcPath = "$projectDir/src/main/"
        project.delete(files(srcPath))
    }
}