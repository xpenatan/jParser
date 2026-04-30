plugins {
    id("com.android.library")
}

val moduleName = "runtime-jni"

val isPublishingTask = gradle.startParameter.taskNames.any { it.contains("publish", ignoreCase = true) }

val androidLibDir = "$projectDir/../runtime-build/build/c++/libs/android"

val androidAbiFiles = mapOf(
    "android_arm64_v8a" to "$androidLibDir/arm64-v8a/libidl.so",
    "android_armeabi_v7a" to "$androidLibDir/armeabi-v7a/libidl.so",
    "android_x86" to "$androidLibDir/x86/libidl.so",
    "android_x86_64" to "$androidLibDir/x86_64/libidl.so",
)

val androidAbiJars = androidAbiFiles.mapNotNull { (classifier, filePath) ->
    if(file(filePath).exists()) {
        tasks.register<Jar>("nativeJar${classifier}") {
            from(filePath)
            archiveClassifier.set(classifier)
        }
    }
    else {
        null
    }
}

val androidAllAbiJar = tasks.register<Jar>("nativeJarAndroid") {
    archiveClassifier.set("android")
    androidAbiFiles.values.forEach { filePath ->
        val nativeFile = file(filePath)
        if(nativeFile.exists()) {
            // Keep ABI folders so same lib filename from each ABI can coexist in the jar.
            from(nativeFile) {
                into(nativeFile.parentFile.name)
            }
        }
    }
}

val filterJniLibs by tasks.registering(Copy::class) {
    into(layout.buildDirectory.dir("tmp/jniLibs"))

    // Prevent stale copied natives from leaking between publish/non-publish builds.
    doFirst {
        delete(layout.buildDirectory.dir("tmp/jniLibs"))
    }

    if(!isPublishingTask) {
        from("$projectDir/../runtime-build/build/c++/libs/android")
        include("**/*.so")
        exclude("**/*.a")
    }
}

android {
    namespace = "com.github.xpenatan.jparser.idl.helper"
    compileSdk = 36

    defaultConfig {
        minSdk = 21
    }

    sourceSets {
        named("main") {
            jniLibs.srcDirs(layout.buildDirectory.dir("tmp/jniLibs"))
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.toVersion(LibExt.javaMainTarget)
        targetCompatibility = JavaVersion.toVersion(LibExt.javaMainTarget)
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
}

tasks.named("preBuild").configure {
    dependsOn(filterJniLibs)
}

dependencies {
    implementation(project(":idl:api:api-core"))
    implementation(project(":loader:loader-core"))
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("maven") {
                artifactId = moduleName
                group = LibExt.groupId
                version = LibExt.libVersion
                artifact(androidAllAbiJar)
                androidAbiJars.forEach { artifact(it) }
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