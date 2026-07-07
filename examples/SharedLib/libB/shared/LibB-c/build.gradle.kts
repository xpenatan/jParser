plugins {
    id("java-library")
}

java {
    sourceCompatibility = JavaVersion.toVersion(LibExt.javaWebTarget)
    targetCompatibility = JavaVersion.toVersion(LibExt.javaWebTarget)
}

dependencies {
    api(project(":examples:SharedLib:libA:shared:LibA-c"))
    api(project(":jParser:api:api-core"))
    api(project(":jParser:loader:loader-core"))
    api(project(":jParser:runtime:shared:runtime-c"))
    api("org.teavm:teavm-core:${LibExt.teaVMVersion}")
}

val libBTeaVMCBuildTask = LibExt.hostBuildProjectTask(":examples:SharedLib:libB:builder", "LibB", "teavm_c")

tasks.named("compileJava") {
    dependsOn(libBTeaVMCBuildTask)
}

tasks.named("clean") {
    doFirst {
        val srcPath = "$projectDir/src/main/java"
        project.delete(files(srcPath))
    }
}
