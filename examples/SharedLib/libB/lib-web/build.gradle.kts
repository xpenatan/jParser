plugins {
    id("java-library")
}

java {
    sourceCompatibility = JavaVersion.toVersion(LibExt.javaWebTarget)
    targetCompatibility = JavaVersion.toVersion(LibExt.javaWebTarget)
}

val emscriptenJS = "$projectDir/../lib-build/build/c++/libs/emscripten/LibB.js"
val emscriptenWASM = "$projectDir/../lib-build/build/c++/libs/emscripten/LibB.wasm"

tasks.jar {
    from(emscriptenJS, emscriptenWASM)
}

dependencies {
    implementation("org.teavm:teavm-jso:${LibExt.teaVMVersion}")
    implementation("org.teavm:teavm-classlib:${LibExt.teaVMVersion}")

    implementation(project(":examples:SharedLib:libA:lib-web"))
    implementation(project(":loader:loader-web"))

    implementation(project(":loader:loader-core"))

    implementation(project(":idl:api:api-web"))
    api(project(":idl:runtime:runtime-web"))
}

tasks.named("clean") {
    doFirst {
        val srcPath = "$projectDir/src/main/java"
        project.delete(files(srcPath))
    }
}