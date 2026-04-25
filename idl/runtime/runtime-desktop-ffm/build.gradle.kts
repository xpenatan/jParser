plugins {
    id("java")
}

val moduleName = "runtime-desktop-ffm"

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

val nativeRuntime by configurations.creating {
    isCanBeConsumed = true
    isCanBeResolved = false
}

artifacts {
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
                            // attach native jars created at top-level
                            nativeJars.forEach { artifact(it) }
        }
    }
}
