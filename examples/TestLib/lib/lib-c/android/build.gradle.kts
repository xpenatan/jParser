plugins {
    id("com.android.library")
}

val teavmCLibsDir = "$projectDir/../../lib-build/build/c++/libs/android"
val stagedJniLibsDir = layout.buildDirectory.dir("generated/teavmCJniLibs")

val stageTeaVMCJniLibs by tasks.registering(Copy::class) {
    listOf("x86", "x86_64", "armeabi-v7a", "arm64-v8a").forEach { abi ->
        from("$teavmCLibsDir/$abi/teavm_c") {
            include("*.so")
            into(abi)
        }
    }
    into(stagedJniLibsDir)
}

android {
    namespace = "com.github.xpenatan.jparser.example.testlib.c"
    compileSdk = 36

    defaultConfig {
        minSdk = 29
    }

    sourceSets {
        named("main") {
            jniLibs.srcDirs(stagedJniLibsDir)
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.toVersion(LibExt.javaMainTarget)
        targetCompatibility = JavaVersion.toVersion(LibExt.javaMainTarget)
    }
}

tasks.matching { task ->
    task.name == "mergeReleaseJniLibFolders" || task.name == "mergeDebugJniLibFolders"
}.configureEach {
    dependsOn(stageTeaVMCJniLibs)
}
