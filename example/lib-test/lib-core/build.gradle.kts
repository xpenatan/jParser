plugins {
    id("java")
}

dependencies {
    if(LibExt.exampleUseRepoLibs) {
        implementation("com.github.xpenatan.jParser:loader-core:${LibExt.libVersion}")
    }
    else {
        implementation(project(":jParser:loader:loader-core"))
    }
}

tasks.named("clean") {
    doFirst {
        val srcPath = "$projectDir/src/main/"
        project.delete(files(srcPath))
    }
}