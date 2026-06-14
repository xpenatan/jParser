plugins {
    id("java-library")
}

java {
    sourceCompatibility = JavaVersion.toVersion(LibExt.javaWebTarget)
    targetCompatibility = JavaVersion.toVersion(LibExt.javaWebTarget)
}

val emscriptenJS = "$projectDir/../lib-build/build/c++/libs/emscripten/LibA.js"
val emscriptenWASM = "$projectDir/../lib-build/build/c++/libs/emscripten/LibA.wasm"

tasks.jar {
    from(emscriptenJS, emscriptenWASM)
}

dependencies {
    implementation(project(":jParser:loader:loader-web"))

    implementation(project(":jParser:loader:loader-core"))
    implementation(project(":jParser:api:api-web"))
    api(project(":jParser:runtime:runtime-web"))
}

tasks.named("clean") {
    doFirst {
        val srcPath = "$projectDir/src/main/java"
        project.delete(files(srcPath))
    }
}