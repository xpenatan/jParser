plugins {
    id("java")
}

val moduleName = "runtime-desktop-jni"

val libDir = "${projectDir}/../runtime-build/build/c++/libs"
val windowsFile = "$libDir/windows/vc/jni/idl64.dll"
val linuxFile = "$libDir/linux/jni/libidl64.so"
val macFile = "$libDir/mac/jni/libidl64.dylib"
val macArmFile = "$libDir/mac/arm/jni/libidlarm64.dylib"

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
        val srcPath = "$projectDir/src/main/"
        project.delete(files(srcPath))
    }
}

java {
    sourceCompatibility = JavaVersion.toVersion(LibExt.java8Target)
    targetCompatibility = JavaVersion.toVersion(LibExt.java8Target)
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
            // publishing will attach the native jars created earlier
            nativeJars.forEach { artifact(it) }
        }
    }
}
