plugins {
    alias(libs.plugins.androidLibrary)
}

val moduleName = "runtime-android"

val androidLibDir = "$projectDir/../../builder/build/c++/libs/android"

data class AndroidNativeAbi(
    val abi: String,
    val artifactSuffix: String,
    val nativeFile: String,
)

val androidAbis = listOf(
    AndroidNativeAbi("x86", "x86", "$androidLibDir/x86/libruntime.so"),
    AndroidNativeAbi("x86_64", "x86_64", "$androidLibDir/x86_64/libruntime.so"),
    AndroidNativeAbi("armeabi-v7a", "armeabi_v7a", "$androidLibDir/armeabi-v7a/libruntime.so"),
    AndroidNativeAbi("arm64-v8a", "arm64_v8a", "$androidLibDir/arm64-v8a/libruntime.so"),
)

val taskNames = gradle.startParameter.taskNames
fun isTaskRequested(taskName: String): Boolean {
    return taskNames.any { it == taskName || it.endsWith(":$taskName") }
}
val isPreparePublishingTask = isTaskRequested("prepareRelease") || isTaskRequested("prepareSnapshot")
val isPublishTask = taskNames.any { it.contains("publish", ignoreCase = true) }
val includeNativesInMainAar = !(isPreparePublishingTask || isPublishTask)

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
                package="com.github.xpenatan.jparser.runtime" >

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
    description = "Build Android JNI runtime wrapper AAR."
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
        description = "Build native-only Android runtime AAR for ${abi.abi}."
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
                throw GradleException("Missing Android native library for ${abi.abi}: ${abi.nativeFile}")
            }
        }
    }
}

android {
    namespace = "com.github.xpenatan.jparser.runtime"
    compileSdk = 36
    enableKotlin = false

    defaultConfig {
        minSdk = 21
    }

    sourceSets {
        named("main") {
            if(includeNativesInMainAar) {
                jniLibs.directories.add(androidLibDir)
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.toVersion(libs.versions.javaMain.get())
        targetCompatibility = JavaVersion.toVersion(libs.versions.javaMain.get())
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    publishing {
        singleVariant("release")
    }
}

dependencies {
    api(project(":jParser:runtime:shared:runtime-jni"))
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifactId = moduleName
            groupId = project.group.toString()
            version = project.version.toString()
            artifact(mainAar)
            pom.withXml {
                val dependenciesNode = asNode().appendNode("dependencies")
                val dependencyNode = dependenciesNode.appendNode("dependency")
                dependencyNode.appendNode("groupId", project.group.toString())
                dependencyNode.appendNode("artifactId", "runtime-jni")
                dependencyNode.appendNode("version", project.version.toString())
                dependencyNode.appendNode("scope", "compile")
            }
        }

        nativeAars.forEach { (abi, nativeAar) ->
            create<MavenPublication>("mavenNative_${abi.artifactSuffix}") {
                artifactId = "${moduleName}_${abi.artifactSuffix}"
                groupId = project.group.toString()
                version = project.version.toString()
                artifact(nativeAar)
            }
        }
    }
}

tasks.named("clean") {
    doFirst {
        val srcPath = "$projectDir/src/main/"
        project.delete(files(srcPath))
    }
}
