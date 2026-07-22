plugins {
    id("java-library")
}

val moduleName = "runtime-desktop-jni"

val libDir = "${projectDir}/../../builder/build/c++/libs"
val windowsFile = "$libDir/windows/vc/jni/runtime64.dll"
val linuxFile = "$libDir/linux/jni/libruntime64.so"
val macFile = "$libDir/mac/jni/libruntime64.dylib"
val macArmFile = "$libDir/mac/arm/jni/libruntimearm64.dylib"

val platforms: Map<String, String> = mapOf(
    "windows_x64" to windowsFile,
    "linux_x64" to linuxFile,
    "mac_x64" to macFile,
    "mac_arm64" to macArmFile,
)

val taskNames = gradle.startParameter.taskNames
fun isTaskRequested(taskName: String): Boolean {
    return taskNames.any { it == taskName || it.endsWith(":$taskName") }
}
val isPreparePublishingTask = isTaskRequested("prepareRelease") || isTaskRequested("prepareSnapshot")
val isPublishTask = taskNames.any { it.contains("publish", ignoreCase = true) }
val includeNativesInMainJar = !(isPreparePublishingTask || isPublishTask)

dependencies {
    api(project(":jParser:runtime:shared:runtime-jni"))
}

val nativeJars = platforms.map { (platform, nativeFile) ->
    platform to tasks.register<Jar>("nativeJar_${platform}") {
        from(nativeFile)
        archiveBaseName.set("${moduleName}-${platform}")
        archiveClassifier.set("")
        doFirst {
            if(!file(nativeFile).isFile) {
                logger.warn("Missing desktop JNI native library for $platform: $nativeFile")
            }
        }
    }
}

tasks.named<Jar>("jar") {
    archiveBaseName.set(moduleName)
    archiveClassifier.set("")
    if(includeNativesInMainJar) {
        platforms.values.forEach { from(it) }
    }
}

java {
    sourceCompatibility = JavaVersion.toVersion(LibExt.javaMainTarget)
    targetCompatibility = JavaVersion.toVersion(LibExt.javaMainTarget)
}

val nativeRuntime by configurations.creating {
    isCanBeConsumed = true
    isCanBeResolved = false
}

artifacts {
    nativeJars.forEach { add(nativeRuntime.name, it.second) }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifactId = moduleName
            groupId = LibExt.groupId
            version = LibExt.libVersion
            artifact(tasks.named("jar"))
            pom.withXml {
                val dependenciesNode = asNode().appendNode("dependencies")
                val dependencyNode = dependenciesNode.appendNode("dependency")
                dependencyNode.appendNode("groupId", LibExt.groupId)
                dependencyNode.appendNode("artifactId", "runtime-jni")
                dependencyNode.appendNode("version", LibExt.libVersion)
                dependencyNode.appendNode("scope", "compile")
            }
        }

        nativeJars.forEach { (platform, nativeJar) ->
            create<MavenPublication>("mavenNative_${platform}") {
                artifactId = "${moduleName}_${platform}"
                groupId = LibExt.groupId
                version = LibExt.libVersion
                artifact(nativeJar)
            }
        }
    }
}
