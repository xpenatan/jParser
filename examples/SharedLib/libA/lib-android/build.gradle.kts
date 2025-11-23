plugins {
    id("com.android.library")
    kotlin("android")
}

group = "jparser.lib.android"

android {
    namespace = "com.github.xpenatan.jparser.example.libA"
    compileSdk = 36

    defaultConfig {
        minSdk = 21
    }

    sourceSets {
        named("main") {
            jniLibs.srcDirs("$projectDir/../lib-build/build/c++/libs/android")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.toVersion(LibExt.java8Target)
        targetCompatibility = JavaVersion.toVersion(LibExt.java8Target)
    }
    kotlinOptions {
        jvmTarget = LibExt.java8Target
    }
}

dependencies {
}