plugins {
    id("com.android.application")
}

android {
    namespace = "com.github.xpenatan.jParser.example.sharedlib.android"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.github.xpenatan.jParser.example.sharedlib.android"
        minSdk = 24
        versionCode = 1
        versionName = "1.0"
    }

    sourceSets {
        named("main") {
//            assets.srcDirs(project.file("../assets"))
            jniLibs.srcDirs("libs")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.toVersion(LibExt.java8Target)
        targetCompatibility = JavaVersion.toVersion(LibExt.java8Target)
    }
}
val natives: Configuration by configurations.creating

dependencies {
    implementation("com.badlogicgames.gdx:gdx:${LibExt.gdxVersion}")
    implementation("com.badlogicgames.gdx:gdx-backend-android:${LibExt.gdxVersion}")

    natives("com.badlogicgames.gdx:gdx-platform:${LibExt.gdxVersion}:natives-armeabi-v7a")
    natives("com.badlogicgames.gdx:gdx-platform:${LibExt.gdxVersion}:natives-arm64-v8a")
    natives("com.badlogicgames.gdx:gdx-platform:${LibExt.gdxVersion}:natives-x86_64")
    natives("com.badlogicgames.gdx:gdx-platform:${LibExt.gdxVersion}:natives-x86")

    implementation(project(":examples:SharedLib:app:core"))
    implementation(project(":examples:SharedLib:libA:lib-android"))
    implementation(project(":examples:SharedLib:libB:lib-android"))
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
        dependsOn("copyAndroidNatives")
    }
}