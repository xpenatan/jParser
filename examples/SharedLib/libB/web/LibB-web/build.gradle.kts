plugins {
    id("java-library")
}

java {
    sourceCompatibility = JavaVersion.toVersion(libs.versions.javaWeb.get())
    targetCompatibility = JavaVersion.toVersion(libs.versions.javaWeb.get())
}

val emscriptenJS = "$projectDir/../../builder/build/c++/libs/emscripten/LibB.js"
val emscriptenWASM = "$projectDir/../../builder/build/c++/libs/emscripten/LibB.wasm"

tasks.named("compileJava") {
    dependsOn(":examples:SharedLib:libB:builder:LibB_build_project")
}

tasks.jar {
    from(emscriptenJS, emscriptenWASM)
}

dependencies {
    implementation(project(":examples:SharedLib:libA:web:LibA-web"))
    implementation(project(":jParser:loader:loader-web"))
    implementation(project(":jParser:loader:loader-core"))

    implementation(project(":jParser:api:api-web"))
    api(project(":jParser:runtime:web:runtime-web"))
}

tasks.named("clean") {
    doFirst {
        val srcPath = "$projectDir/src/main/java"
        project.delete(files(srcPath))
    }
}
