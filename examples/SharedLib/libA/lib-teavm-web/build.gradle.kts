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
    implementation("org.teavm:teavm-jso:${LibExt.teaVMVersion}")
    implementation("org.teavm:teavm-classlib:${LibExt.teaVMVersion}")

    implementation(project(":loader:loader-teavm-web"))
    implementation(project(":loader:loader-core"))
    implementation(project(":idl:api:api-teavm-web"))
    api(project(":idl:runtime:runtime-teavm-web"))
}

tasks.named("clean") {
    doFirst {
        val srcPath = "$projectDir/src/main/java"
        project.delete(files(srcPath))
    }
}