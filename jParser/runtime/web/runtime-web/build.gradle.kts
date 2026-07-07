plugins {
    id("java-library")
}

val moduleName = "runtime-web"

val emscriptenJS = "$projectDir/../../builder/build/c++/libs/emscripten/runtime.js"
val emscriptenWASM = "$projectDir/../../builder/build/c++/libs/emscripten/runtime.wasm"

val taskNames = gradle.startParameter.taskNames
fun isTaskRequested(taskName: String): Boolean {
    return taskNames.any { it == taskName || it.endsWith(":$taskName") }
}
val isPrepareDeployTask = isTaskRequested("prepareReleaseDeploy") || isTaskRequested("prepareSnapshotDeploy")
val isPublishTask = taskNames.any { it.contains("publish", ignoreCase = true) }
val includeNativesInMainJar = !(isPrepareDeployTask || isPublishTask)

val wasmJar = tasks.register<Jar>("wasmJar") {
    // Publish web runtime payload as a standalone wasm artifact.
    from(emscriptenJS, emscriptenWASM)
    archiveBaseName.set("${moduleName}-wasm")
    archiveClassifier.set("")
}

tasks.named("compileJava") {
    dependsOn(":jParser:runtime:builder:runtime_helper_build_project")
}

tasks.named<Jar>("jar") {
    // For in-repo project dependencies, keep classes and web payload in the same jar.
    // During publishing, keep main runtime-web artifact classes-only.
    if(includeNativesInMainJar) {
        from(emscriptenJS, emscriptenWASM)
    }
}

dependencies {
    api(project(":jParser:api:api-core"))
    api(project(":jParser:api:api-web"))
    api(project(":jParser:loader:loader-core"))
    api(project(":jParser:loader:loader-web"))

    api("org.teavm:teavm-tooling:${LibExt.teaVMVersion}")
    api("org.teavm:teavm-jso:${LibExt.teaVMVersion}")
    api("org.teavm:teavm-classlib:${LibExt.teaVMVersion}")
    api("org.teavm:teavm-jso-apis:${LibExt.teaVMVersion}")
    api("org.teavm:teavm-jso-impl:${LibExt.teaVMVersion}")
    api("org.teavm:teavm-extension-spi:${LibExt.teaVMVersion}")
}

sourceSets {
    main {
        java.setSrcDirs(listOf("src/main/java", "src/main/support/java"))
        resources.setSrcDirs(listOf("src/main/resources"))
    }
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
