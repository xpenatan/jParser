import org.gradle.api.tasks.compile.JavaCompile

plugins {
    id("base")
}

val teavmBuild: Configuration by configurations.creating
val iosResources: Configuration by configurations.creating
val teavmClassesDir = layout.buildDirectory.dir("teavmBuild/classes")
val iosProjectDir = layout.buildDirectory.dir("ios-c")
val teavmOutputDir = iosProjectDir.map { it.dir("c/src") }
val iosTeaVMRuntimeOverlayDir = iosProjectDir.map {
    it.dir("c/external_cpp/jparser/runtime/teavm/ios")
}
val iosTeaVMRuntimeOverlayFiles = listOf("definitions.h", "fiber.c")
val cmakeBuildDir = iosProjectDir.map { it.dir("cmake-simulator") }
val hostArchitecture = System.getProperty("os.arch").lowercase()
val defaultSimulatorArchitecture = if(hostArchitecture.contains("aarch64") || hostArchitecture.contains("arm64")) {
    "arm64"
}
else {
    "x86_64"
}
val simulatorArchitecture = providers.gradleProperty("iosSimulatorArch")
    .orElse(defaultSimulatorArchitecture)

dependencies {
    teavmBuild(project(":examples:SharedLib:app:core"))
    teavmBuild(project(":examples:SharedLib:libA:shared:LibA-c"))
    teavmBuild(project(":examples:SharedLib:libB:shared:LibB-c"))
    teavmBuild(project(":jParser:runtime:shared:runtime-c"))
    teavmBuild("org.teavm:teavm-tooling:${LibExt.teaVMVersion}")
    teavmBuild("org.teavm:teavm-classlib:${LibExt.teaVMVersion}")

    iosResources(project(":jParser:runtime:ios:runtime-ios-c"))
    iosResources(project(":examples:SharedLib:libB:ios:LibB-ios-c"))
}

val compileTeaVMBuildJava by tasks.registering(JavaCompile::class) {
    source = fileTree("src/teavm/java")
    destinationDirectory.set(teavmClassesDir)
    classpath = teavmBuild
    sourceCompatibility = LibExt.javaWebTarget
    targetCompatibility = LibExt.javaWebTarget
}

val generateTeaVMC by tasks.registering(JavaExec::class) {
    group = "example-ios"
    description = "Generate SharedLib TeaVM C sources for the custom iOS emulator"
    dependsOn(compileTeaVMBuildJava)
    mainClass.set("BuildTeaVMC")
    classpath = files(teavmClassesDir) + teavmBuild
    workingDir = projectDir
}

val extractIosResources by tasks.registering(Copy::class) {
    group = "example-ios"
    description = "Extract packaged jParser TeaVM C resources for the custom iOS emulator"
    dependsOn(iosResources)
    from({
        iosResources.map { artifact ->
            if(artifact.isDirectory) artifact else zipTree(artifact)
        }
    }) {
        include("external_cpp/**")
    }
    into(iosProjectDir.map { it.dir("c") })
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    includeEmptyDirs = false
}

val stageIosSources by tasks.registering {
    dependsOn(generateTeaVMC, extractIosResources)
    inputs.file("src/main/c/app_include.c")
    inputs.files(iosTeaVMRuntimeOverlayDir.map { overlayDir ->
        iosTeaVMRuntimeOverlayFiles.map { overlayDir.file(it).asFile }
    })
    outputs.files(teavmOutputDir.map { outputDir ->
        listOf("app_include.c", *iosTeaVMRuntimeOverlayFiles.toTypedArray())
            .map { outputDir.file(it).asFile }
    })
    doFirst {
        val overlayDir = iosTeaVMRuntimeOverlayDir.get().asFile
        iosTeaVMRuntimeOverlayFiles.forEach { fileName ->
            if(!overlayDir.resolve(fileName).isFile) {
                throw GradleException("Missing packaged iOS TeaVM C runtime overlay: $fileName")
            }
        }
    }
    doLast {
        val outputDir = teavmOutputDir.get().asFile
        val overlayDir = iosTeaVMRuntimeOverlayDir.get().asFile
        outputDir.mkdirs()
        file("src/main/c/app_include.c")
            .copyTo(outputDir.resolve("app_include.c"), overwrite = true)
        iosTeaVMRuntimeOverlayFiles.forEach { fileName ->
            overlayDir.resolve(fileName)
                .copyTo(outputDir.resolve(fileName), overwrite = true)
        }
    }
}

val prepareIosCProject by tasks.registering {
    group = "example-ios"
    description = "Prepare the generated sources and packaged resources used by the custom iOS project"
    dependsOn(stageIosSources)
}

val configureIosSimulator by tasks.registering(Exec::class) {
    group = "example-ios"
    description = "Configure the custom SharedLib iOS emulator project"
    dependsOn(prepareIosCProject)
    doFirst {
        if(!System.getProperty("os.name").lowercase().contains("mac")) {
            throw GradleException("The custom iOS emulator project requires macOS and Xcode.")
        }
    }
    commandLine(
        "cmake",
        "-S", projectDir.absolutePath,
        "-B", cmakeBuildDir.get().asFile.absolutePath,
        "-G", "Xcode",
        "-DCMAKE_SYSTEM_NAME=iOS",
        "-DCMAKE_OSX_SYSROOT=iphonesimulator",
        "-DCMAKE_OSX_ARCHITECTURES=${simulatorArchitecture.get()}",
        "-DCMAKE_OSX_DEPLOYMENT_TARGET=14.0",
        "-DCMAKE_XCODE_ATTRIBUTE_CODE_SIGNING_ALLOWED=NO",
        "-DCMAKE_XCODE_ATTRIBUTE_CODE_SIGNING_REQUIRED=NO",
    )
}

val buildIosSimulator by tasks.registering(Exec::class) {
    group = "example-ios"
    description = "Build the custom SharedLib TeaVM C iOS emulator app"
    dependsOn(configureIosSimulator)
    commandLine(
        "cmake",
        "--build", cmakeBuildDir.get().asFile.absolutePath,
        "--config", "Debug",
        "--",
        "-sdk", "iphonesimulator",
        "CODE_SIGNING_ALLOWED=NO",
    )
}

tasks.register("SharedLib_build_app_ios_c") {
    group = "example-ios"
    description = "Build the custom SharedLib iOS emulator application backed by TeaVM C"
    dependsOn(buildIosSimulator)
}
