plugins {
    id("java")
    id("java-library")
}

// NOTE: Generated FFM Java code requires Java 24+ to compile (uses java.lang.foreign.*).
// Set to Java 11 as a placeholder so Gradle can configure the module even without Java 24 installed.
java {
    sourceCompatibility = JavaVersion.toVersion(LibExt.java11Target)
    targetCompatibility = JavaVersion.toVersion(LibExt.java11Target)
}

dependencies {
    if(LibExt.exampleUseRepoLibs) {
        api("com.github.xpenatan.jParser:loader-core:-SNAPSHOT")
        api("com.github.xpenatan.jParser:idl-core:-SNAPSHOT")
        api("com.github.xpenatan.jParser:idl-helper-core:-SNAPSHOT")
    }
    else {
        api(project(":loader:loader-core"))
        api(project(":idl:idl-core"))
        api(project(":idl-helper:idl-helper-core"))
    }
}

tasks.named("clean") {
    doFirst {
        val srcPath = "$projectDir/src/main/"
        project.delete(files(srcPath))
    }
}

