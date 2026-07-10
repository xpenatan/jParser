plugins {
    id("java-library")
}

java {
    sourceCompatibility = JavaVersion.toVersion(LibExt.javaWebTarget)
    targetCompatibility = JavaVersion.toVersion(LibExt.javaWebTarget)
}

dependencies {
    api(project(":examples:TestLib:lib:core"))
    api(project(":jParser:api:api-core"))
    api(project(":jParser:loader:loader-core"))
    api(project(":jParser:runtime:shared:runtime-c"))
    api("org.teavm:teavm-core:${LibExt.teaVMVersion}")
}

val testLibGenerationTask = ":examples:TestLib:lib:builder:TestLib_build_project"

tasks.named("compileJava") {
    dependsOn(testLibGenerationTask)
}

tasks.named("processResources") {
    dependsOn(testLibGenerationTask)
}

tasks.named("clean") {
    doFirst {
        val srcPath = "$projectDir/src/main/java"
        project.delete(files(srcPath))
    }
}

sourceSets {
    main {
        java.setSrcDirs(listOf("src/main/java"))
        resources.setSrcDirs(listOf("src/main/resources", "build/generated/jparser/resources/main"))
    }
}
