plugins {
    alias(libs.plugins.androidApplication)
}

android {
    namespace = "com.github.xpenatan.jParser.example.sharedlib.android"
    compileSdk = 36
    enableKotlin = false

    defaultConfig {
        applicationId = "com.github.xpenatan.jParser.example.sharedlib.android"
        minSdk = 29
        versionCode = 1
        versionName = "1.0"
    }

    sourceSets {
        named("main") {
//            assets.srcDirs(project.file("../assets"))
            jniLibs.directories.add("libs")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.toVersion(libs.versions.javaMain.get())
        targetCompatibility = JavaVersion.toVersion(libs.versions.javaMain.get())
    }
}
val natives: Configuration by configurations.creating

dependencies {
    implementation(libs.gdxCore)
    implementation(libs.gdxBackendAndroid)

    natives(variantOf(libs.gdxPlatform) { classifier("natives-armeabi-v7a") })
    natives(variantOf(libs.gdxPlatform) { classifier("natives-arm64-v8a") })
    natives(variantOf(libs.gdxPlatform) { classifier("natives-x86_64") })
    natives(variantOf(libs.gdxPlatform) { classifier("natives-x86") })

    implementation(project(":examples:SharedLib:app:core"))
    api(project(":examples:SharedLib:libA:android:LibA-android"))
    api(project(":examples:SharedLib:libB:android:LibB-android"))
}


tasks.register("copyAndroidNatives") {
    doFirst {
        natives.files.forEach { jar ->
            val outputDir = file("libs/" + jar.nameWithoutExtension.substringAfterLast("natives-"))
            outputDir.mkdirs()
            copy {
                from(zipTree(jar))
                into(outputDir)
                include("*.so")
            }
        }
    }
}

tasks.whenTaskAdded {
    if ("package" in name) {
        dependsOn(
            "copyAndroidNatives",
            ":jParser:runtime:builder:runtime_helper_build_project_android_jni",
            ":examples:SharedLib:libA:builder:LibA_build_project_android_jni",
            ":examples:SharedLib:libB:builder:LibB_build_project_android_jni"
        )
    }
}
