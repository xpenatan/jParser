plugins {
    id("java-library")
}

java {
    sourceCompatibility = JavaVersion.toVersion(libs.versions.javaWeb.get())
    targetCompatibility = JavaVersion.toVersion(libs.versions.javaWeb.get())
}

dependencies {
    api(project(":examples:SharedLib:libA:shared:LibA-c"))
    api(project(":examples:SharedLib:libB:core"))
    api(project(":jParser:api:api-core"))
    api(project(":jParser:loader:loader-core"))
    api(project(":jParser:runtime:shared:runtime-c"))
    api(libs.teavmCore)
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
