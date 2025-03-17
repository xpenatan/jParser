plugins {
    id("com.android.library")
    kotlin("android")
}

group = "jparser.lib.android"

android {
    namespace = "com.github.xpenatan.jparser.example.testlib"
    compileSdk = 34

    defaultConfig {
        minSdk = 21
    }

    sourceSets {
        named("main") {
            jniLibs.srcDirs("$projectDir/../lib-build/build/c++/libs/android")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
}