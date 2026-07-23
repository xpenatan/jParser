plugins {
    alias(libs.plugins.androidLibrary)
}

val teavmCLibsDir = "$projectDir/../../builder/build/c++/libs/android"
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
    namespace = "libB.c"
    compileSdk = 36
    enableKotlin = false

    defaultConfig {
        minSdk = 29
    }

    sourceSets {
        named("main") {
            jniLibs.directories.add(stagedJniLibsDir.get().asFile.absolutePath)
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.toVersion(libs.versions.javaMain.get())
        targetCompatibility = JavaVersion.toVersion(libs.versions.javaMain.get())
    }
}

tasks.matching { task ->
    task.name == "mergeReleaseJniLibFolders" || task.name == "mergeDebugJniLibFolders"
}.configureEach {
    dependsOn(stageTeaVMCJniLibs)
}
