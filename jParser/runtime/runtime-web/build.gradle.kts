plugins {
    id("java-library")
}

val moduleName = "runtime-web"

val emscriptenJS = "$projectDir/../runtime-build/build/c++/libs/emscripten/runtime.js"
val emscriptenWASM = "$projectDir/../runtime-build/build/c++/libs/emscripten/runtime.wasm"

val wasmJar = tasks.register<Jar>("wasmJar") {
    // Publish web runtime payload as a standalone wasm artifact.
    from(emscriptenJS, emscriptenWASM)
    archiveBaseName.set("${moduleName}-wasm")
    archiveClassifier.set("")
}


val taskNames = gradle.startParameter.taskNames
fun isTaskRequested(taskName: String): Boolean {
    return taskNames.any { it == taskName || it.endsWith(":$taskName") }
}
val isPrepareDeployTask = isTaskRequested("prepareReleaseDeploy") || isTaskRequested("prepareSnapshotDeploy")
val isPublishTask = taskNames.any { it.contains("publish", ignoreCase = true) }
val includeNativesInMainJar = !(isPrepareDeployTask || isPublishTask)

tasks.named<Jar>("jar") {
    // For in-repo project dependencies, keep classes and web payload in the same jar.
    // During publishing, keep main runtime-web artifact classes-only.
    if(includeNativesInMainJar) {
        from(emscriptenJS, emscriptenWASM)
    }
}

dependencies {
    implementation(project(":jParser:api:api-web"))
    implementation(project(":jParser:loader:loader-core"))
    implementation(project(":jParser:loader:loader-web"))

    api("org.teavm:teavm-tooling:${LibExt.teaVMVersion}")
    api("org.teavm:teavm-jso:${LibExt.teaVMVersion}")
    api("org.teavm:teavm-classlib:${LibExt.teaVMVersion}")
    api("org.teavm:teavm-jso-apis:${LibExt.teaVMVersion}")
    api("org.teavm:teavm-jso-impl:${LibExt.teaVMVersion}")
    api("org.teavm:teavm-extension-spi:${LibExt.teaVMVersion}")
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