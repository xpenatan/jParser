plugins {
    id("java")
}

val moduleName = "runtime-desktop-ffm"

val libDir = "${projectDir}/../../builder/build/c++/libs"
val windowsFile = "$libDir/windows/vc/ffm/runtime64.dll"
val linuxFile = "$libDir/linux/ffm/libruntime64.so"
val macFile = "$libDir/mac/ffm/libruntime64.dylib"
val macArmFile = "$libDir/mac/arm/ffm/libruntimearm64.dylib"

val taskNames = gradle.startParameter.taskNames
fun isTaskRequested(taskName: String): Boolean {
    return taskNames.any { it == taskName || it.endsWith(":$taskName") }
}
val isPreparePublishingTask = isTaskRequested("prepareRelease") || isTaskRequested("prepareSnapshot")
val isPublishTask = taskNames.any { it.contains("publish", ignoreCase = true) }
val includeNativesInMainJar = !(isPreparePublishingTask || isPublishTask)

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
        from(nativeFile)
        archiveBaseName.set("${moduleName}-${platform}")
        archiveClassifier.set("")
    }
}

tasks.named("compileJava") {
    dependsOn(":jParser:runtime:builder:runtime_helper_build_project")
}

tasks.named<Jar>("jar") {
    // For in-repo project dependencies, keep classes and native payload in the same jar.
    // During publishing, keep main runtime-desktop-ffm artifact classes-only.
    if(includeNativesInMainJar) {
        from(windowsFile, linuxFile, macFile, macArmFile)
    }
}

val nativeRuntime by configurations.creating {
    isCanBeConsumed = true
    isCanBeResolved = false
}

artifacts {
    nativeJars.forEach { add(nativeRuntime.name, it.second) }
}

tasks.named("clean") {
    doFirst {
        val srcPath = "$projectDir/src/main/java"
        project.delete(files(srcPath))
    }
}

java {
    sourceCompatibility = JavaVersion.toVersion(libs.versions.javaFfm.get())
    targetCompatibility = JavaVersion.toVersion(libs.versions.javaFfm.get())
}

java {
    withJavadocJar()
    withSourcesJar()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifactId = moduleName
            from(components["java"])
        }

        nativeJars.forEach { (platform, nativeJar) ->
            create<MavenPublication>("mavenNative_${platform}") {
                artifactId = "${moduleName}_${platform}"
                artifact(nativeJar)
            }
        }
    }
}
