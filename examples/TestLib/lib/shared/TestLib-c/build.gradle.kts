plugins {
    id("java-library")
}

java {
    sourceCompatibility = JavaVersion.toVersion(LibExt.javaWebTarget)
    targetCompatibility = JavaVersion.toVersion(LibExt.javaWebTarget)
}

dependencies {
    api(project(":jParser:api:api-core"))
    api(project(":jParser:loader:loader-core"))
    api(project(":jParser:runtime:shared:runtime-c"))
    api("org.teavm:teavm-core:${LibExt.teaVMVersion}")
}

val testLibTeaVMCBuildTask = LibExt.hostBuildProjectTask(":examples:TestLib:lib:builder", "TestLib", "teavm_c")

tasks.named("compileJava") {
    dependsOn(testLibTeaVMCBuildTask)
}

tasks.named("clean") {
    doFirst {
        val srcPath = "$projectDir/src/main/java"
        project.delete(files(srcPath))
    }
}
