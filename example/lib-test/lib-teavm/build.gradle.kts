plugins {
    id("java")
}

val emscriptenFile = "$projectDir/../lib-build/build/c++/libs/emscripten/test.wasm.js"

tasks.jar {
    from(emscriptenFile)
}

dependencies {
    implementation("org.teavm:teavm-jso:${LibExt.teaVMVersion}")
    implementation(project(":jParser:loader:loader-teavm"))
    implementation("org.teavm:teavm-classlib:${LibExt.teaVMVersion}")

    if(LibExt.exampleUseRepoLibs) {
        implementation("com.github.xpenatan.jParser:loader-core:${LibExt.libVersion}")
    }
    else {
        implementation(project(":jParser:loader:loader-core"))
    }
    testImplementation(project(":example:lib-test:lib-core"))
    testImplementation("junit:junit:${LibExt.jUnitVersion}")
    testImplementation("org.teavm:teavm-core:${LibExt.teaVMVersion}")
    testImplementation("org.teavm:teavm-jso-apis:${LibExt.teaVMVersion}")
    testImplementation("org.teavm:teavm-classlib:${LibExt.teaVMVersion}")
    testImplementation("org.teavm:teavm-junit:${LibExt.teaVMVersion}")
}

tasks.named("clean") {
    doFirst {
        val srcPath = "$projectDir/src/main/java/gen"
        project.delete(files(srcPath))
    }
}