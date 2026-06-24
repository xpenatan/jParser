plugins {
    id("java")
}

val moduleName = "runtime-jni"

val libDir = "${projectDir}/../../runtime-build/build/c++/libs"
val windowsFile = "$libDir/windows/vc/jni/runtime64.dll"
val linuxFile = "$libDir/linux/jni/libruntime64.so"
val macFile = "$libDir/mac/jni/libruntime64.dylib"
val macArmFile = "$libDir/mac/arm/jni/libruntimearm64.dylib"

dependencies {
    implementation(project(":jParser:api:api-core"))
    implementation(project(":jParser:loader:loader-core"))
}

// Build per-platform native jars as standalone artifacts (no Maven classifier usage).
val platforms: MutableMap<String, Jar.() -> Unit> = mutableMapOf()
if(file(windowsFile).exists()) {
    platforms["windows_x64"] = { from(windowsFile) }
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

val nativeJars = platforms.map { (platform, config) ->
    platform to tasks.register<Jar>("nativeJar_${platform}") {
        config()
        archiveBaseName.set("${moduleName}-${platform}")
        archiveClassifier.set("")
    }
}

val nativeDesktopJar = tasks.register<Jar>("nativeJarDesktop") {
    archiveBaseName.set("${moduleName}-desktop")
    archiveClassifier.set("")
    listOf(
        "windows_x64" to windowsFile,
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

val nativeFiles = listOf(windowsFile, linuxFile, macFile, macArmFile).map(::file).filter { it.exists() }

val taskNames = gradle.startParameter.taskNames
fun isTaskRequested(taskName: String): Boolean {
    return taskNames.any { it == taskName || it.endsWith(":$taskName") }
}
val isPrepareDeployTask = isTaskRequested("prepareReleaseDeploy") || isTaskRequested("prepareSnapshotDeploy")
val isPublishTask = taskNames.any { it.contains("publish", ignoreCase = true) }
val includeNativesInMainJar = !(isPrepareDeployTask || isPublishTask)

tasks.named<Jar>("jar") {
    // For in-repo project dependencies, keep classes and native payload in the same jar.
    // During publishing, keep main runtime-jni artifact classes-only.
    if(includeNativesInMainJar) {
        from(nativeFiles)
    }
}

val nativeRuntime by configurations.creating {
    isCanBeConsumed = true
    isCanBeResolved = false
}

artifacts {
    add(nativeRuntime.name, nativeDesktopJar)
    nativeJars.forEach { add(nativeRuntime.name, it.second) }
}

tasks.named("clean") {
    doFirst {
        val srcPath = "$projectDir/src/main/"
        project.delete(files(srcPath))
    }
}

java {
    sourceCompatibility = JavaVersion.toVersion(LibExt.javaMainTarget)
    targetCompatibility = JavaVersion.toVersion(LibExt.javaMainTarget)
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
        }

        create<MavenPublication>("mavenDesktopNative") {
            artifactId = "${moduleName}-desktop"
            group = LibExt.groupId
            version = LibExt.libVersion
            artifact(nativeDesktopJar)
        }

        nativeJars.forEach { (platform, nativeJar) ->
            create<MavenPublication>("mavenNative_${platform}") {
                artifactId = "${moduleName}_${platform}"
                group = LibExt.groupId
                version = LibExt.libVersion
                artifact(nativeJar)
            }
        }
    }
}
