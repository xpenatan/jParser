        plugins {
    id("java")
    id("java-library")
}

java {
    sourceCompatibility = JavaVersion.toVersion(LibExt.java8Target)
    targetCompatibility = JavaVersion.toVersion(LibExt.java8Target)
}

dependencies {
    if(LibExt.exampleUseRepoLibs) {
        api("com.github.xpenatan.jParser:loader-core:-SNAPSHOT")
        api("com.github.xpenatan.jParser:idl-core:-SNAPSHOT")
    }
    else {
        api(project(":loader:loader-core"))
        api(project(":idl:idl-core"))
    }
    api(project(":idl-helper:idl-helper-core"))
}

tasks.named("clean") {
    doFirst {
        val srcPath = "$projectDir/src/main/"
        project.delete(files(srcPath))
    }
}