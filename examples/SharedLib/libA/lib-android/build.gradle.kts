plugins {
    id("com.android.library")
}

val filterJniLibs by tasks.registering(Copy::class) {
    from("$projectDir/../lib-build/build/c++/libs/android")
    into(layout.buildDirectory.dir("tmp/jniLibs"))
    include("**/*.so")
    exclude("**/*.a")
}

tasks.named("preBuild").configure {
    dependsOn(filterJniLibs)
}

android {
    namespace = "com.github.xpenatan.jparser.example.libA"
    compileSdk = 36

    defaultConfig {
        minSdk = 29
    }

    sourceSets {
        named("main") {
            jniLibs.srcDirs("$projectDir/../lib-build/build/c++/libs/android")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.toVersion(LibExt.javaMainTarget)
        targetCompatibility = JavaVersion.toVersion(LibExt.javaMainTarget)
    }
}

dependencies {
    implementation(project(":loader:loader-core"))
    implementation(project(":idl:api:api-core"))
    implementation(project(":idl:runtime:runtime-android"))
    implementation(project(":examples:SharedLib:libA:lib-jni"))
}

tasks.named("clean") {
    doFirst {
        val srcPath = "$projectDir/src/main/"
        project.delete(files(srcPath))
    }
}