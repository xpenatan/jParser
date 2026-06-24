plugins {
    id("com.android.library")
}

val moduleName = "runtime-c_android"
val teavmCLibsDir = "$projectDir/../../runtime-build/build/c++/libs/android"
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
    namespace = "com.github.xpenatan.jparser.runtime.c"
    compileSdk = 36

    defaultConfig {
        minSdk = 21
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

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    publishing {
        singleVariant("release")
    }
}

tasks.matching { task ->
    task.name == "mergeReleaseJniLibFolders" || task.name == "mergeDebugJniLibFolders"
}.configureEach {
    dependsOn(stageTeaVMCJniLibs)
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
