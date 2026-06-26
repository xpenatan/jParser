        plugins {
    id("java")
    id("java-library")
}

java {
    sourceCompatibility = JavaVersion.toVersion(LibExt.javaMainTarget)
    targetCompatibility = JavaVersion.toVersion(LibExt.javaMainTarget)
}

dependencies {

    implementation(project(":examples:SharedLib:libA:lib-core"))

    api(project(":jParser:loader:loader-core"))

    api(project(":jParser:api:api-core"))
    implementation(project(":jParser:runtime:runtime-core"))
}

tasks.named("compileJava") {
    dependsOn(":examples:SharedLib:libB:plugin:jParser_generate")
}

tasks.named("clean") {
    doFirst {
        val srcPath = "$projectDir/src/main/"
        project.delete(files(srcPath))
    }
}
