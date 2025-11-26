plugins {
    id("java-library")
}

java {
    sourceCompatibility = JavaVersion.toVersion(LibExt.java11Target)
    targetCompatibility = JavaVersion.toVersion(LibExt.java11Target)
}

val emscriptenFile = "$projectDir/../lib-build/build/c++/libs/emscripten/LibA.wasm.js"

tasks.jar {
    from(emscriptenFile)
}


dependencies {
    implementation("org.teavm:teavm-jso:${LibExt.teaVMVersion}")
    implementation("org.teavm:teavm-classlib:${LibExt.teaVMVersion}")

    if(LibExt.exampleUseRepoLibs) {
        implementation("com.github.xpenatan.jParser:loader-teavm:-SNAPSHOT")
        implementation("com.github.xpenatan.jParser:loader-core:-SNAPSHOT")
        implementation("com.github.xpenatan.jParser:idl-teavm:-SNAPSHOT")
    }
    else {
        implementation(project(":loader:loader-teavm"))
        implementation(project(":loader:loader-core"))
        implementation(project(":idl:idl-teavm"))
    }
}

tasks.named("clean") {
    doFirst {
        val srcPath = "$projectDir/src/main/java"
        project.delete(files(srcPath))
    }
}