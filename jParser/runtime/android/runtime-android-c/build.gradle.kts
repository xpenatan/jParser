plugins {
    id("com.android.library")
}

val moduleName = "runtime-android-c"

val teavmCLibsDir = "$projectDir/../../builder/build/c++/libs/android"
val stagedJniLibsDir = layout.buildDirectory.dir("generated/teavmCJniLibs")

data class AndroidNativeAbi(
    val abi: String,
    val artifactSuffix: String,
    val nativeFile: String,
)

val androidAbis = listOf(
    AndroidNativeAbi("x86", "x86", "$teavmCLibsDir/x86/teavm_c/libruntime.so"),
    AndroidNativeAbi("x86_64", "x86_64", "$teavmCLibsDir/x86_64/teavm_c/libruntime.so"),
    AndroidNativeAbi("armeabi-v7a", "armeabi_v7a", "$teavmCLibsDir/armeabi-v7a/teavm_c/libruntime.so"),
    AndroidNativeAbi("arm64-v8a", "arm64_v8a", "$teavmCLibsDir/arm64-v8a/teavm_c/libruntime.so"),
)

val taskNames = gradle.startParameter.taskNames
fun isTaskRequested(taskName: String): Boolean {
    return taskNames.any { it == taskName || it.endsWith(":$taskName") }
}
val isPrepareDeployTask = isTaskRequested("prepareReleaseDeploy") || isTaskRequested("prepareSnapshotDeploy")
val isPublishTask = taskNames.any { it.contains("publish", ignoreCase = true) }
val includeNativesInMainAar = !(isPrepareDeployTask || isPublishTask)

val nativeAarManifest = layout.buildDirectory.file("generated/nativeAar/AndroidManifest.xml")
val generateNativeAarManifest by tasks.registering {
    outputs.file(nativeAarManifest)
    doLast {
        val manifestFile = nativeAarManifest.get().asFile
        manifestFile.parentFile.mkdirs()
        manifestFile.writeText(
            """
            <?xml version="1.0" encoding="utf-8"?>
            <manifest xmlns:android="http://schemas.android.com/apk/res/android"
                package="com.github.xpenatan.jparser.runtime.c" >

                <uses-sdk android:minSdkVersion="21" />

            </manifest>
            """.trimIndent() + System.lineSeparator()
        )
    }
}

val emptyAndroidClassesJar by tasks.registering(Jar::class) {
    archiveFileName.set("classes.jar")
    destinationDirectory.set(layout.buildDirectory.dir("generated/nativeAar/emptyClasses"))
}

val mainAar by tasks.registering(Zip::class) {
    group = "build"
    description = "Build Android TeaVM C runtime wrapper AAR."
    archiveBaseName.set(moduleName)
    archiveClassifier.set("")
    archiveExtension.set("aar")
    dependsOn(generateNativeAarManifest)
    dependsOn(emptyAndroidClassesJar)
    from(nativeAarManifest)
    from(emptyAndroidClassesJar.flatMap { it.archiveFile }) {
        rename { "classes.jar" }
    }
}

val nativeAars = androidAbis.map { abi ->
    abi to tasks.register<Zip>("nativeAar_${abi.artifactSuffix}") {
        group = "build"
        description = "Build native-only Android TeaVM C runtime AAR for ${abi.abi}."
        archiveBaseName.set("${moduleName}-${abi.artifactSuffix}")
        archiveClassifier.set("")
        archiveExtension.set("aar")
        dependsOn(generateNativeAarManifest)
        dependsOn(emptyAndroidClassesJar)
        from(nativeAarManifest)
        from(emptyAndroidClassesJar.flatMap { it.archiveFile }) {
            rename { "classes.jar" }
        }
        from(abi.nativeFile) {
            into("jni/${abi.abi}")
        }
        doFirst {
            if(!file(abi.nativeFile).isFile) {
                throw GradleException("Missing Android TeaVM C native library for ${abi.abi}: ${abi.nativeFile}")
            }
        }
    }
}

val stageTeaVMCJniLibs by tasks.registering(Copy::class) {
    androidAbis.forEach { abi ->
        from(file(abi.nativeFile).parentFile) {
            include("*.so")
            into(abi.abi)
        }
    }
    into(stagedJniLibsDir)
}

android {
    namespace = "com.github.xpenatan.jparser.runtime.c"
    compileSdk = 36

    defaultConfig {
        minSdk = 21
    }

    sourceSets {
        named("main") {
            if(includeNativesInMainAar) {
                jniLibs.srcDirs(stagedJniLibsDir)
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.toVersion(LibExt.javaMainTarget)
        targetCompatibility = JavaVersion.toVersion(LibExt.javaMainTarget)
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    publishing {
        singleVariant("release")
    }
}

tasks.matching { task ->
    task.name == "mergeReleaseJniLibFolders" || task.name == "mergeDebugJniLibFolders"
}.configureEach {
    dependsOn(stageTeaVMCJniLibs)
}

dependencies {
    api(project(":jParser:runtime:shared:runtime-c"))
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifactId = moduleName
            groupId = LibExt.groupId
            version = LibExt.libVersion
            artifact(mainAar)
            pom.withXml {
                val dependenciesNode = asNode().appendNode("dependencies")
                val dependencyNode = dependenciesNode.appendNode("dependency")
                dependencyNode.appendNode("groupId", LibExt.groupId)
                dependencyNode.appendNode("artifactId", "runtime-c")
                dependencyNode.appendNode("version", LibExt.libVersion)
                dependencyNode.appendNode("scope", "compile")
            }
        }

        nativeAars.forEach { (abi, nativeAar) ->
            create<MavenPublication>("mavenNative_${abi.artifactSuffix}") {
                artifactId = "${moduleName}_${abi.artifactSuffix}"
                groupId = LibExt.groupId
                version = LibExt.libVersion
                artifact(nativeAar)
            }
        }
    }
}
