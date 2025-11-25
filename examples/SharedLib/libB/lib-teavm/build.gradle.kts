plugins {
    id("java-library")
}

java {
    sourceCompatibility = JavaVersion.toVersion(LibExt.java11Target)
    targetCompatibility = JavaVersion.toVersion(LibExt.java11Target)
}

val emscriptenFile = "$projectDir/../lib-build/build/c++/libs/emscripten/LibB.wasm.js"

tasks.jar {
    from(emscriptenFile)
}


dependencies {
    implementation("org.teavm:teavm-jso:${LibExt.teaVMVersion}")
    implementation("org.teavm:teavm-classlib:${LibExt.teaVMVersion}")

    implementation(project(":examples:SharedLib:libA:lib-teavm"))

    if(LibExt.exampleUseRepoLibs) {
        implementation("com.github.xpenatan.jParser:loader-teavm:-SNAPSHOT")
        implementation("com.github.xpenatan.jParser:loader-core:-SNAPSHOT")
        implementation("com.github.xpenatan.jParser:idl-teavm:-SNAPSHOT")
        implementation("com.github.xpenatan.jParser:idl-core:-SNAPSHOT")
    }
    else {
        implementation(project(":loader:loader-teavm"))
        implementation(project(":loader:loader-core"))
        api(project(":idl:idl-teavm"))
//        api(project(":idl:idl-core"))
    }
}

tasks.named("clean") {
    doFirst {
        val srcPath = "$projectDir/src/main/java"
        project.delete(files(srcPath))
    }
}