plugins {
    id("java-library")
}

java {
    sourceCompatibility = JavaVersion.toVersion(LibExt.javaWebTarget)
    targetCompatibility = JavaVersion.toVersion(LibExt.javaWebTarget)
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
    implementation(project(":examples:SharedLib:libA:web:wasm"))
    implementation(project(":jParser:loader:loader-web"))
    implementation(project(":jParser:loader:loader-core"))

    implementation(project(":jParser:api:api-web"))
    api(project(":jParser:runtime:web:wasm"))
}

tasks.named("clean") {
    doFirst {
        val srcPath = "$projectDir/src/main/java"
        project.delete(files(srcPath))
    }
}
