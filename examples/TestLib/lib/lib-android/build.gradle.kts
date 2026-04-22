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
    namespace = "com.github.xpenatan.jparser.example.testlib"
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
        sourceCompatibility = JavaVersion.toVersion(LibExt.java8Target)
        targetCompatibility = JavaVersion.toVersion(LibExt.java8Target)
    }
}

dependencies {
    api(project(":loader:loader-core"))
    api(project(":idl:idl-core"))
    api(project(":idl-helper:idl-helper-android"))
}

tasks.named("clean") {
    doFirst {
        val srcPath = "$projectDir/src/main/"
        project.delete(files(srcPath))
    }
}
