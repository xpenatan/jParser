plugins {
    alias(libs.plugins.androidLibrary)
}

val filterJniLibs by tasks.registering(Copy::class) {
    from("$projectDir/../../builder/build/c++/libs/android")
    into(layout.buildDirectory.dir("tmp/jniLibs"))
    include("**/*.so")
    exclude("**/*.a")
}

tasks.named("preBuild").configure {
    dependsOn(filterJniLibs)
}

android {
    namespace = "com.github.xpenatan.jparser.example.testlib"
    compileSdk = 36
    enableKotlin = false

    defaultConfig {
        minSdk = 29
    }

    sourceSets {
        named("main") {
            jniLibs.directories.add("$projectDir/../../builder/build/c++/libs/android")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.toVersion(libs.versions.javaMain.get())
        targetCompatibility = JavaVersion.toVersion(libs.versions.javaMain.get())
    }
}

dependencies {
    api(project(":jParser:loader:loader-core"))
    api(project(":jParser:api:api-core"))
    api(project(":jParser:runtime:android:runtime-android"))
    api(project(":examples:TestLib:lib:shared:TestLib-jni"))
}

tasks.named("clean") {
    doFirst {
        val srcPath = "$projectDir/src/main/"
        project.delete(files(srcPath))
    }
}
