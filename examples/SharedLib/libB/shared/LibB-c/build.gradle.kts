plugins {
    id("java-library")
}

java {
    sourceCompatibility = JavaVersion.toVersion(LibExt.javaWebTarget)
    targetCompatibility = JavaVersion.toVersion(LibExt.javaWebTarget)
}

dependencies {
    api(project(":examples:SharedLib:libA:shared:LibA-c"))
    api(project(":examples:SharedLib:libB:core"))
    api(project(":jParser:api:api-core"))
    api(project(":jParser:loader:loader-core"))
    api(project(":jParser:runtime:shared:runtime-c"))
    api("org.teavm:teavm-core:${LibExt.teaVMVersion}")
}

val libBGenerationTask = ":examples:SharedLib:libB:builder:LibB_build_project"

tasks.named("compileJava") {
    dependsOn(libBGenerationTask)
}

tasks.named("processResources") {
    dependsOn(libBGenerationTask)
}

sourceSets {
    main {
        java.setSrcDirs(listOf("src/main/java"))
        java.include("gen/c/**")
        resources.setSrcDirs(listOf("src/main/resources", "build/generated/jparser/resources/main"))
    }
}

tasks.named("clean") {
    doFirst {
        val srcPath = "$projectDir/src/main/java"
        project.delete(files(srcPath))
    }
}
