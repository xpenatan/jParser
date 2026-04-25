plugins {
    id("java-library")
}

java {
    sourceCompatibility = JavaVersion.toVersion(LibExt.java11Target)
    targetCompatibility = JavaVersion.toVersion(LibExt.java11Target)
}

val emscriptenJS = "$projectDir/../lib-build/build/c++/libs/emscripten/LibB.js"
val emscriptenWASM = "$projectDir/../lib-build/build/c++/libs/emscripten/LibB.wasm"

tasks.jar {
    from(emscriptenJS, emscriptenWASM)
}

dependencies {
    implementation("org.teavm:teavm-jso:${LibExt.teaVMVersion}")
    implementation("org.teavm:teavm-classlib:${LibExt.teaVMVersion}")

    implementation(project(":examples:SharedLib:libA:lib-teavm"))

    implementation(project(":loader:loader-teavm"))
    implementation(project(":loader:loader-core"))

    implementation(project(":idl:api:api-teavm"))
    api(project(":idl:runtime:runtime-teavm"))
}

tasks.named("clean") {
    doFirst {
        val srcPath = "$projectDir/src/main/java"
        project.delete(files(srcPath))
    }
}