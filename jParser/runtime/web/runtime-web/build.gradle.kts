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
val isPreparePublishingTask = isTaskRequested("prepareRelease") || isTaskRequested("prepareSnapshot")
val isPublishTask = taskNames.any { it.contains("publish", ignoreCase = true) }
val includeNativesInMainJar = !(isPreparePublishingTask || isPublishTask)

val wasmJar = tasks.register<Jar>("wasmJar") {
    // Publish web runtime payload as a standalone wasm artifact.
    from(emscriptenJS, emscriptenWASM)
    archiveBaseName.set("${moduleName}-wasm")
    archiveClassifier.set("")
    doFirst {
        val missingFiles = listOf(emscriptenJS, emscriptenWASM).filterNot { file(it).isFile }
        if(missingFiles.isNotEmpty()) {
            logger.warn(
                "Missing TeaVM web runtime payloads:\n" + missingFiles.joinToString("\n")
            )
        }
    }
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

    api(libs.teavmTooling)
    api(libs.bundles.teavmWeb)
}

sourceSets {
    main {
        java.setSrcDirs(listOf("src/main/java", "src/main/support/java"))
        resources.setSrcDirs(listOf("src/main/resources"))
    }
}

java {
    sourceCompatibility = JavaVersion.toVersion(libs.versions.javaWeb.get())
    targetCompatibility = JavaVersion.toVersion(libs.versions.javaWeb.get())
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
            from(components["java"])
        }

        create<MavenPublication>("mavenWasm") {
            artifactId = "${moduleName}_wasm"
            artifact(wasmJar)
        }
    }
}
