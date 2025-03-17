plugins {
    id("java")
    id("java-library")
}

dependencies {
    if(LibExt.exampleUseRepoLibs) {
        api("com.github.xpenatan.jParser:loader-core:${LibExt.libVersion}")
    }
    else {
        api(project(":jParser:loader:loader-core"))
    }
}

tasks.named("clean") {
    doFirst {
        val srcPath = "$projectDir/src/main/"
        project.delete(files(srcPath))
    }
}