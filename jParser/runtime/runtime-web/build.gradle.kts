plugins {
    id("java-library")
}

val moduleName = "runtime_web"

val emscriptenJS = "$projectDir/../runtime-build/build/c++/libs/emscripten/runtime.js"
val emscriptenWASM = "$projectDir/../runtime-build/build/c++/libs/emscripten/runtime.wasm"

val wasmJar = tasks.register<Jar>("wasmJar") {
    // Publish web runtime payload as a standalone wasm artifact.
    from(emscriptenJS, emscriptenWASM)
    archiveBaseName.set("${moduleName}_wasm")
    archiveClassifier.set("")
}

val isPublishingTask = gradle.startParameter.taskNames.any { it.contains("publish", ignoreCase = true) }

tasks.named<Jar>("jar") {
    // For in-repo project dependencies, keep classes and web payload in the same jar.
    // During publishing, keep main runtime_web artifact classes-only.
    if(!isPublishingTask) {
        from(emscriptenJS, emscriptenWASM)
    }
}

dependencies {
    implementation(project(":jParser:api:api-web"))
    implementation(project(":jParser:loader:loader-core"))
    implementation(project(":jParser:loader:loader-web"))

    api("org.teavm:teavm-jso:${LibExt.teaVMVersion}")
    api("org.teavm:teavm-classlib:${LibExt.teaVMVersion}")
    api("org.teavm:teavm-jso:${LibExt.teaVMVersion}")
    api("org.teavm:teavm-jso-apis:${LibExt.teaVMVersion}")
    api("org.teavm:teavm-jso-impl:${LibExt.teaVMVersion}")
}

java {
    sourceCompatibility = JavaVersion.toVersion(LibExt.javaWebTarget)
    targetCompatibility = JavaVersion.toVersion(LibExt.javaWebTarget)
}

java {
    withJavadocJar()
    withSourcesJar()
}

tasks.named("clean") {
    doFirst {
        val srcPath = "$projectDir/src/main/java"
        project.delete(files(srcPath))
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifactId = moduleName
            group = LibExt.groupId
            version = LibExt.libVersion
            from(components["java"])
        }

        create<MavenPublication>("mavenWasm") {
            artifactId = "${moduleName}_wasm"
            group = LibExt.groupId
            version = LibExt.libVersion
            artifact(wasmJar)
        }
    }
}