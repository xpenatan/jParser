import org.gradle.api.tasks.compile.JavaCompile

plugins {
    id("com.android.application")
}

val teavmBuild: Configuration by configurations.creating
val appJniLibsDir = layout.buildDirectory.dir("generated/jniLibs")
val teavmClassesDir = layout.buildDirectory.dir("teavmBuild/classes")
val teavmOutputDir = layout.buildDirectory.dir("teavm-c")

dependencies {
    teavmBuild(project(":examples:TestLib:app:core"))
    teavmBuild(project(":examples:TestLib:lib:lib-c:core"))
    teavmBuild(project(":jParser:runtime:runtime-c:core"))
    teavmBuild("org.teavm:teavm-tooling:${LibExt.teaVMVersion}")
    teavmBuild("org.teavm:teavm-classlib:${LibExt.teaVMVersion}")

    implementation(project(":jParser:runtime:runtime-c:android"))
    implementation(project(":examples:TestLib:lib:lib-c:android"))
}

android {
    namespace = "com.github.xpenatan.jParser.example.testlib.androidc"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.github.xpenatan.jParser.example.testlib.androidc"
        minSdk = 29
        versionCode = 1
        versionName = "1.0"
    }

    sourceSets {
        named("main") {
            jniLibs.srcDirs(appJniLibsDir)
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.toVersion(LibExt.javaMainTarget)
        targetCompatibility = JavaVersion.toVersion(LibExt.javaMainTarget)
    }
}

val compileTeaVMBuildJava by tasks.registering(JavaCompile::class) {
    source = fileTree("src/teavm/java")
    destinationDirectory.set(teavmClassesDir)
    classpath = teavmBuild
    sourceCompatibility = LibExt.javaWebTarget
    targetCompatibility = LibExt.javaWebTarget
}

val generateTeaVMC by tasks.registering(JavaExec::class) {
    group = "example-android"
    description = "Generate TestLib Android TeaVM C app sources"
    dependsOn(compileTeaVMBuildJava)
    mainClass.set("BuildTeaVMC")
    classpath = files(teavmClassesDir) + teavmBuild
    workingDir = projectDir
}

val generateTeaVMCImportHeader by tasks.registering {
    dependsOn(
        generateTeaVMC,
        ":jParser:runtime:runtime-build:runtime_helper_build_project_android_teavm_c",
        ":examples:TestLib:lib:lib-build:TestLib_build_project_android_teavm_c"
    )
    outputs.file(teavmOutputDir.map { it.file("teavmc_imports.h") })
    doLast {
        val headers = listOf(
            rootProject.file("jParser/runtime/runtime-build/build/c++/src/teavmcglue/TeaVMCGlue.h"),
            rootProject.file("examples/TestLib/lib/lib-build/build/c++/src/teavmcglue/TeaVMCGlue.h"),
        )
        val seen = linkedSetOf<String>()
        val lines = mutableListOf(
            "#pragma once",
            "#include <stdint.h>",
            "#include <stdbool.h>",
            ""
        )
        headers.forEach { header ->
            Regex("typedef\\s+[^;]*\\(\\*fp_[^;]+;").findAll(header.readText()).forEach { match ->
                val line = match.value.trim().replace(Regex("\\s+"), " ")
                if(seen.add(line)) {
                    lines.add(line)
                }
            }
        }
        lines.add("")
        headers.forEach { header ->
            Regex("TEAVMC_EXPORT\\s+([A-Za-z_][A-Za-z0-9_:<>\\*\\s]*?)\\s+([A-Za-z_][A-Za-z0-9_]*)\\s*\\(([^)]*)\\)\\s*\\{").findAll(header.readText()).forEach { match ->
                val returnType = match.groupValues[1].trim().replace(Regex("\\s+"), " ")
                val name = match.groupValues[2].trim()
                val params = match.groupValues[3].trim()
                if(!Regex("::|&|Native|std::").containsMatchIn(returnType)) {
                    val line = "$returnType $name($params);"
                    if(seen.add(line)) {
                        lines.add(line)
                    }
                }
            }
        }
        val outputFile = teavmOutputDir.get().file("teavmc_imports.h").asFile
        outputFile.parentFile.mkdirs()
        outputFile.writeText(lines.joinToString(System.lineSeparator()))
    }
}

data class AndroidAbi(
    val abi: String,
    val target: String
)

val androidAbis = listOf(
    AndroidAbi("arm64-v8a", "aarch64-linux-android29"),
    AndroidAbi("armeabi-v7a", "armv7a-linux-androideabi29"),
    AndroidAbi("x86", "i686-linux-android29"),
    AndroidAbi("x86_64", "x86_64-linux-android29"),
)

fun ndkClang(): String {
    val ndkHome = System.getenv("ANDROID_NDK_HOME") ?: error("ANDROID_NDK_HOME is required for android-c")
    val osFolder = when {
        org.gradle.internal.os.OperatingSystem.current().isWindows -> "windows-x86_64"
        org.gradle.internal.os.OperatingSystem.current().isMacOsX -> "darwin-x86_64"
        else -> "linux-x86_64"
    }
    val executable = if(org.gradle.internal.os.OperatingSystem.current().isWindows) "clang.exe" else "clang"
    return "$ndkHome/toolchains/llvm/prebuilt/$osFolder/bin/$executable"
}

fun ndkSysroot(): String {
    val ndkHome = System.getenv("ANDROID_NDK_HOME") ?: error("ANDROID_NDK_HOME is required for android-c")
    val osFolder = when {
        org.gradle.internal.os.OperatingSystem.current().isWindows -> "windows-x86_64"
        org.gradle.internal.os.OperatingSystem.current().isMacOsX -> "darwin-x86_64"
        else -> "linux-x86_64"
    }
    return "$ndkHome/toolchains/llvm/prebuilt/$osFolder/sysroot"
}

val buildTeaVMCAndroidNative by tasks.registering {
    group = "example-android"
    description = "Compile TestLib TeaVM C app into Android native libraries"
    dependsOn(generateTeaVMCImportHeader)
    androidAbis.forEach { abi ->
        outputs.file(appJniLibsDir.map { it.file("${abi.abi}/libTestLibTeaVMCApp.so") })
    }
    doLast {
        val clang = ndkClang()
        val generatedDir = teavmOutputDir.get().asFile
        val appBridge = project.file("src/main/c/testlib_android_bridge.c")
        val shim = project.file("src/main/c/android-teavm-shim.h")
        val importHeader = File(generatedDir, "teavmc_imports.h")
        androidAbis.forEach { abi ->
            val outDir = appJniLibsDir.get().dir(abi.abi).asFile
            outDir.mkdirs()
            val command = listOf(
                clang,
                "--target=${abi.target}",
                "--sysroot=${ndkSysroot()}",
                "-shared",
                "-fPIC",
                "-O0",
                "-std=c11",
                "-Wno-nonportable-include-path",
                "-Wno-parentheses-equality",
                "-Wno-unused-value",
                "-Wno-incompatible-pointer-types-discards-qualifiers",
                "-include", shim.absolutePath,
                "-include", importHeader.absolutePath,
                File(generatedDir, "all.c").absolutePath,
                appBridge.absolutePath,
                "-L${rootProject.file("jParser/runtime/runtime-build/build/c++/libs/android/${abi.abi}/teavm_c").absolutePath}",
                "-L${rootProject.file("examples/TestLib/lib/lib-build/build/c++/libs/android/${abi.abi}/teavm_c").absolutePath}",
                "-lruntime",
                "-lTestLib",
                "-llog",
                "-lm",
                "-Wl,-soname,libTestLibTeaVMCApp.so",
                "-Wl,-z,max-page-size=16384",
                "-o", File(outDir, "libTestLibTeaVMCApp.so").absolutePath
            )
            logger.lifecycle("Compiling TestLib Android-C native app for ${abi.abi}")
            val process = ProcessBuilder(command)
                .directory(projectDir)
                .redirectErrorStream(true)
                .start()
            val output = process.inputStream.bufferedReader().readText()
            val exitCode = process.waitFor()
            if(exitCode != 0) {
                logger.error(output)
                throw GradleException("Android TeaVM C native compile failed for ${abi.abi} with exit code $exitCode")
            }
            if(output.isNotBlank()) {
                logger.info(output)
            }
        }
    }
}

tasks.matching { task ->
    task.name == "mergeDebugJniLibFolders" || task.name == "mergeReleaseJniLibFolders"
}.configureEach {
    dependsOn(buildTeaVMCAndroidNative)
}

tasks.register("TestLib_build_app_android_c") {
    group = "example-android"
    description = "Build TestLib Android app backed by TeaVM C"
    dependsOn("assembleDebug")
}
