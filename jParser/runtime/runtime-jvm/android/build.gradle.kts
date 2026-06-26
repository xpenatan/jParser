plugins {
    id("com.android.library")
}

val moduleName = "runtime-android"

android {
    namespace = "com.github.xpenatan.jparser.runtime"
    compileSdk = 36

    defaultConfig {
        minSdk = 21
    }

    sourceSets {
        named("main") {
            jniLibs.srcDirs("$projectDir/../../runtime-build/build/c++/libs/android")
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

    publishing {
        singleVariant("release")
    }
}

dependencies {
    implementation(project(":jParser:api:api-core"))
    implementation(project(":jParser:loader:loader-core"))
}

tasks.named("preBuild").configure {
    dependsOn(":jParser:runtime:plugin:jParser_build_android_jni")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifactId = moduleName
            group = LibExt.groupId
            version = LibExt.libVersion
        }
    }
}

afterEvaluate {
    publishing {
        publications.named<MavenPublication>("maven") {
            from(components["release"])
        }
    }
}

tasks.named("clean") {
    doFirst {
        val srcPath = "$projectDir/src/main/"
        project.delete(files(srcPath))
    }
}
