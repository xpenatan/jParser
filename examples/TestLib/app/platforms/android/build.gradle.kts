plugins {
    alias(libs.plugins.androidApplication)
}

android {
    namespace = "com.example.myapplication"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.myapplication"
        minSdk = 29
        versionCode = 1
        versionName = "1.0"
    }

    sourceSets {
        named("main") {
//            java.srcDirs("src/main/kotlin")
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
    coreLibraryDesugaring(libs.desugarJdkLibs)
    implementation(libs.gdxCore)
    implementation(libs.gdxBackendAndroid)
    natives(variantOf(libs.gdxPlatform) { classifier("natives-armeabi-v7a") })
    natives(variantOf(libs.gdxPlatform) { classifier("natives-arm64-v8a") })
    natives(variantOf(libs.gdxPlatform) { classifier("natives-x86_64") })
    natives(variantOf(libs.gdxPlatform) { classifier("natives-x86") })

    implementation(project(":examples:TestLib:app:core"))
    implementation(project(":examples:TestLib:lib:android:TestLib-android"))
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
            ":examples:TestLib:lib:builder:TestLib_build_project_android_jni"
        )
    }
}
