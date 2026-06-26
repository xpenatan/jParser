plugins {
    id("java")
}

val moduleName = "runtime-ffm"

val libDir = "${projectDir}/../../runtime-build/build/c++/libs"
val windowsFile = "$libDir/windows/vc/ffm/runtime64.dll"
val linuxFile = "$libDir/linux/ffm/libruntime64.so"
val macFile = "$libDir/mac/ffm/libruntime64.dylib"
val macArmFile = "$libDir/mac/arm/ffm/libruntimearm64.dylib"

val nativeBuildTasks: Map<String, String> = mapOf(
    "windows_x64" to ":jParser:runtime:runtime-build:runtime_helper_build_project_windows64_ffm",
    "linux_x64" to ":jParser:runtime:runtime-build:runtime_helper_build_project_linux64_ffm",
    "mac_x64" to ":jParser:runtime:runtime-build:runtime_helper_build_project_mac64_ffm",
    "mac_arm64" to ":jParser:runtime:runtime-build:runtime_helper_build_project_macArm_ffm",
)
val hostNativeBuildTask = nativeBuildTasks.getValue(LibExt.hostDesktopPlatform)

dependencies {
    implementation(project(":jParser:api:api-core"))
    implementation(project(":jParser:loader:loader-core"))
}

// Build per-platform native jars as standalone artifacts (no Maven classifier usage).
val platforms: Map<String, String> = mapOf(
    "windows_x64" to windowsFile,
    "linux_x64" to linuxFile,
    "mac_x64" to macFile,
    "mac_arm64" to macArmFile,
)

val nativeJars = platforms.map { (platform, nativeFile) ->
    platform to tasks.register<Jar>("nativeJar_${platform}") {
        dependsOn(nativeBuildTasks.getValue(platform))
        from(nativeFile)
        archiveBaseName.set("${moduleName}-${platform}")
        archiveClassifier.set("")
    }
}

val nativeDesktopJar = tasks.register<Jar>("nativeJarDesktop") {
    dependsOn(nativeBuildTasks.values)
    archiveBaseName.set("${moduleName}-desktop")
    archiveClassifier.set("")
    platforms.forEach { (folder, path) ->
        from(path) {
            into(folder)
        }
    }
}

val taskNames = gradle.startParameter.taskNames
fun isTaskRequested(taskName: String): Boolean {
    return taskNames.any { it == taskName || it.endsWith(":$taskName") }
}
val isPrepareDeployTask = isTaskRequested("prepareReleaseDeploy") || isTaskRequested("prepareSnapshotDeploy")
val isPublishTask = taskNames.any { it.contains("publish", ignoreCase = true) }
val includeNativesInMainJar = !(isPrepareDeployTask || isPublishTask)

tasks.named("compileJava") {
    dependsOn(":jParser:runtime:runtime-build:runtime_helper_build_project")
}

tasks.named<Jar>("jar") {
    dependsOn(hostNativeBuildTask)
    // For in-repo project dependencies, keep classes and native payload in the same jar.
    // During publishing, keep main runtime-ffm artifact classes-only.
    if(includeNativesInMainJar) {
        from(windowsFile, linuxFile, macFile, macArmFile)
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
