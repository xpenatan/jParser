plugins {
    id("java")
}

val moduleName = "runtime-ffm"

val libDir = "${projectDir}/../runtime-build/build/c++/libs"
val windowsFile = "$libDir/windows/vc/ffm/idl64.dll"
val linuxFile = "$libDir/linux/ffm/libidl64.so"
val macFile = "$libDir/mac/ffm/libidl64.dylib"
val macArmFile = "$libDir/mac/arm/ffm/libidlarm64.dylib"

dependencies {
    implementation(project(":idl:api:api-core"))
    implementation(project(":loader:loader-core"))
}

// create per-platform native jars (classifier-based) similar to jWebGPU
val platforms: MutableMap<String, Jar.() -> Unit> = mutableMapOf()
if(file(windowsFile).exists()) {
    platforms["windows_64"] = { from(windowsFile) }
}
if(file(linuxFile).exists()) {
    platforms["linux_x64"] = { from(linuxFile) }
}
if(file(macFile).exists()) {
    platforms["mac_x64"] = { from(macFile) }
}
if(file(macArmFile).exists()) {
    platforms["mac_arm64"] = { from(macArmFile) }
}

val nativeJars = platforms.map { (classifier, config) ->
    tasks.register<Jar>("nativeJar${classifier}") {
        config()
        archiveClassifier.set(classifier)
    }
}

val nativeDesktopJar = tasks.register<Jar>("nativeJarDesktop") {
    archiveClassifier.set("desktop")
    listOf(
        "windows_64" to windowsFile,
        "linux_x64" to linuxFile,
        "mac_x64" to macFile,
        "mac_arm64" to macArmFile,
    ).forEach { (folder, path) ->
        val nativeFile = file(path)
        if(nativeFile.exists()) {
            from(nativeFile) {
                into(folder)
            }
        }
    }
}

val isPublishingTask = gradle.startParameter.taskNames.any { it.contains("publish", ignoreCase = true) }
val nativeFiles = listOf(windowsFile, linuxFile, macFile, macArmFile).map(::file).filter { it.exists() }

tasks.named<Jar>("jar") {
    // For in-repo project dependencies, keep classes and native payload in the same jar.
    // During publishing, keep main runtime-ffm artifact classes-only.
    if(!isPublishingTask) {
        from(nativeFiles)
    }
}

val nativeRuntime by configurations.creating {
    isCanBeConsumed = true
    isCanBeResolved = false
}

artifacts {
    add(nativeRuntime.name, nativeDesktopJar)
    nativeJars.forEach { add(nativeRuntime.name, it) }
}

tasks.named("clean") {
    doFirst {
        val srcPath = "$projectDir/src/main/java"
        project.delete(files(srcPath))
    }
}

java {
    sourceCompatibility = JavaVersion.toVersion(LibExt.javaFFMTarget)
    targetCompatibility = JavaVersion.toVersion(LibExt.javaFFMTarget)
}

java {
    withJavadocJar()
    withSourcesJar()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifactId = moduleName
            group = LibExt.groupId
            version = LibExt.libVersion
            from(components["java"])
            artifact(nativeDesktopJar)
            // attach native jars created at top-level
            nativeJars.forEach { artifact(it) }
        }
    }
}
