import org.gradle.api.tasks.compile.JavaCompile

plugins {
    id("com.android.application")
}

val teavmBuild: Configuration by configurations.creating
val appJniLibsDir = layout.buildDirectory.dir("generated/jniLibs")
val nativeObjectsDir = layout.buildDirectory.dir("generated/teavmCNativeObjects")
val teavmClassesDir = layout.buildDirectory.dir("teavmBuild/classes")
val teavmOutputDir = layout.buildDirectory.dir("teavm-c")

dependencies {
    teavmBuild(project(":examples:TestLib:app:core"))
    teavmBuild(project(":examples:TestLib:lib:shared:TestLib-c"))
    teavmBuild(project(":jParser:runtime:shared:runtime-c"))
    teavmBuild("org.teavm:teavm-tooling:${LibExt.teaVMVersion}")
    teavmBuild("org.teavm:teavm-classlib:${LibExt.teaVMVersion}")

    implementation(project(":jParser:runtime:android:runtime-android-c"))
    implementation(project(":examples:TestLib:lib:android:TestLib-android-c"))
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
        ":jParser:runtime:builder:runtime_helper_build_project_android_teavm_c",
        ":examples:TestLib:lib:builder:TestLib_build_project_android_teavm_c"
    )
    outputs.file(teavmOutputDir.map { it.file("teavmc_imports.h") })
    doLast {
        val headers = listOf(
            rootProject.file("jParser/runtime/builder/build/c++/src/teavmcglue/TeaVMCGlue.h"),
            rootProject.file("examples/TestLib/lib/builder/build/c++/src/teavmcglue/TeaVMCGlue.h"),
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

fun ndkHome(): String {
    return System.getenv("ANDROID_NDK_HOME")
        ?: System.getenv("ANDROID_NDK_ROOT")
        ?: android.ndkDirectory.absolutePath
}

fun ndkCompiler(tool: String): String {
    val ndkHome = ndkHome()
    val osFolder = when {
        org.gradle.internal.os.OperatingSystem.current().isWindows -> "windows-x86_64"
        org.gradle.internal.os.OperatingSystem.current().isMacOsX -> "darwin-x86_64"
        else -> "linux-x86_64"
    }
    val executable = if(org.gradle.internal.os.OperatingSystem.current().isWindows) "$tool.exe" else tool
    return "$ndkHome/toolchains/llvm/prebuilt/$osFolder/bin/$executable"
}

fun ndkSysroot(): String {
    val ndkHome = ndkHome()
    val osFolder = when {
        org.gradle.internal.os.OperatingSystem.current().isWindows -> "windows-x86_64"
        org.gradle.internal.os.OperatingSystem.current().isMacOsX -> "darwin-x86_64"
        else -> "linux-x86_64"
    }
    return "$ndkHome/toolchains/llvm/prebuilt/$osFolder/sysroot"
}

fun runNativeCommand(description: String, command: List<String>) {
    logger.lifecycle(description)
    val process = ProcessBuilder(command)
        .directory(projectDir)
        .redirectErrorStream(true)
        .start()
    val output = process.inputStream.bufferedReader().readText()
    val exitCode = process.waitFor()
    if(exitCode != 0) {
        logger.error(output)
        throw GradleException("$description failed with exit code $exitCode")
    }
    if(output.isNotBlank()) {
        logger.info(output)
    }
}

val buildTeaVMCAndroidNative by tasks.registering {
    group = "example-android"
    description = "Compile TestLib TeaVM C app into Android native libraries"
    dependsOn(generateTeaVMCImportHeader)
    inputs.file(teavmOutputDir.map { it.file("all.c") })
    inputs.file(teavmOutputDir.map { it.file("teavmc_imports.h") })
    inputs.files(
        project.file("src/main/c/testlib_android_bridge.c"),
        project.file("src/main/c/android-teavm-shim.h"),
        rootProject.file("jParser/loader/loader-c/src/main/resources/external_cpp/jparser/loader/teavmc_loader.h"),
        rootProject.file("jParser/loader/loader-c/src/main/resources/external_cpp/jparser/loader/teavmc_loader.cpp"),
        rootProject.file("jParser/runtime/builder/build/c++/src/teavmcabi/TeaVMCDispatch.cpp"),
        rootProject.file("examples/TestLib/lib/builder/build/c++/src/teavmcabi/TeaVMCDispatch.cpp"),
    )
    androidAbis.forEach { abi ->
        inputs.files(
            rootProject.file("jParser/runtime/builder/build/c++/libs/android/${abi.abi}/teavm_c/libruntime.so"),
            rootProject.file("examples/TestLib/lib/builder/build/c++/libs/android/${abi.abi}/teavm_c/libTestLib.so"),
        )
        outputs.file(appJniLibsDir.map { it.file("${abi.abi}/libTestLibTeaVMCApp.so") })
    }
    doLast {
        val clang = ndkCompiler("clang")
        val clangCpp = ndkCompiler("clang++")
        val generatedDir = teavmOutputDir.get().asFile
        val appBridge = project.file("src/main/c/testlib_android_bridge.c")
        val shim = project.file("src/main/c/android-teavm-shim.h")
        val importHeader = File(generatedDir, "teavmc_imports.h")
        val loaderDir = rootProject.file("jParser/loader/loader-c/src/main/resources/external_cpp/jparser/loader")
        val loaderHeader = File(loaderDir, "teavmc_loader.h")
        val cppSources = listOf(
            "teavmc_loader" to File(loaderDir, "teavmc_loader.cpp"),
            "runtime_dispatch" to rootProject.file("jParser/runtime/builder/build/c++/src/teavmcabi/TeaVMCDispatch.cpp"),
            "testlib_dispatch" to rootProject.file("examples/TestLib/lib/builder/build/c++/src/teavmcabi/TeaVMCDispatch.cpp"),
        )
        androidAbis.forEach { abi ->
            val outDir = appJniLibsDir.get().dir(abi.abi).asFile
            outDir.mkdirs()
            val objectDir = nativeObjectsDir.get().dir(abi.abi).asFile
            objectDir.mkdirs()

            val targetFlags = listOf(
                "--target=${abi.target}",
                "--sysroot=${ndkSysroot()}",
            )
            val cObjects = listOf(
                "teavm_app" to File(generatedDir, "all.c"),
                "android_bridge" to appBridge,
            ).map { (name, source) ->
                val output = File(objectDir, "$name.o")
                val forcedIncludes = if(source == appBridge) {
                    emptyList()
                }
                else {
                    listOf(
                        "-include", shim.absolutePath,
                        "-include", loaderHeader.absolutePath,
                        "-include", importHeader.absolutePath,
                    )
                }
                runNativeCommand(
                    "Compiling TestLib Android-C C source $name for ${abi.abi}",
                    listOf(clang) + targetFlags + listOf(
                        "-fPIC",
                        "-O0",
                        "-std=c11",
                        "-Wno-nonportable-include-path",
                        "-Wno-parentheses-equality",
                        "-Wno-unused-value",
                        "-Wno-incompatible-pointer-types-discards-qualifiers",
                    ) + forcedIncludes + listOf(
                        "-c", source.absolutePath,
                        "-o", output.absolutePath,
                    )
                )
                output
            }

            val cppObjects = cppSources.map { (name, source) ->
                val output = File(objectDir, "$name.o")
                runNativeCommand(
                    "Compiling TestLib Android-C C++ source $name for ${abi.abi}",
                    listOf(clangCpp) + targetFlags + listOf(
                        "-fPIC",
                        "-O0",
                        "-std=c++17",
                        "-DJPARSER_TEAVMC_LINKAGE_MODE=JPARSER_TEAVMC_LINKAGE_SHARED_LINKED",
                        "-I${loaderDir.absolutePath}",
                        "-I${source.parentFile.absolutePath}",
                        "-c", source.absolutePath,
                        "-o", output.absolutePath,
                    )
                )
                output
            }

            val command = listOf(
                clangCpp,
            ) + targetFlags + listOf(
                "-shared",
                "-fPIC",
                "-O0",
            ) + (cObjects + cppObjects).map { it.absolutePath } + listOf(
                "-L${rootProject.file("jParser/runtime/builder/build/c++/libs/android/${abi.abi}/teavm_c").absolutePath}",
                "-L${rootProject.file("examples/TestLib/lib/builder/build/c++/libs/android/${abi.abi}/teavm_c").absolutePath}",
                "-lruntime",
                "-lTestLib",
                "-llog",
                "-ldl",
                "-lm",
                "-static-libstdc++",
                "-Wl,-soname,libTestLibTeaVMCApp.so",
                "-Wl,-z,max-page-size=16384",
                "-o", File(outDir, "libTestLibTeaVMCApp.so").absolutePath
            )
            runNativeCommand("Linking TestLib Android-C native app for ${abi.abi}", command)
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
