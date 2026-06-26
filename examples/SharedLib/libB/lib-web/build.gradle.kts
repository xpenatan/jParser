plugins {
    id("java-library")
}

java {
    sourceCompatibility = JavaVersion.toVersion(LibExt.javaWebTarget)
    targetCompatibility = JavaVersion.toVersion(LibExt.javaWebTarget)
}

val emscriptenJS = "$projectDir/../lib-build/build/c++/libs/emscripten/LibB.js"
val emscriptenWASM = "$projectDir/../lib-build/build/c++/libs/emscripten/LibB.wasm"

tasks.named("compileJava") {
    dependsOn(":examples:SharedLib:libB:plugin:jParser_generate")
}

tasks.jar {
    dependsOn(":examples:SharedLib:libB:plugin:jParser_build_web_wasm")
    from(emscriptenJS, emscriptenWASM)
}

dependencies {
    implementation(project(":examples:SharedLib:libA:lib-web"))
    implementation(project(":jParser:loader:loader-web"))
    implementation(project(":jParser:loader:loader-core"))

    implementation(project(":jParser:api:api-web"))
    api(project(":jParser:runtime:runtime-jvm:web"))
}

tasks.named("clean") {
    doFirst {
        val srcPath = "$projectDir/src/main/java"
        project.delete(files(srcPath))
    }
}
