import org.gradle.nativeplatform.platform.internal.DefaultNativePlatform

plugins {
    id("java")
}

java {
    sourceCompatibility = JavaVersion.toVersion(LibExt.java8Target)
    targetCompatibility = JavaVersion.toVersion(LibExt.java8Target)
}

// Separate configuration for JNI bridge (used by benchmark comparison tasks only).
val jniBridge by configurations.creating {
    isTransitive = true
}

dependencies {
    implementation(project(":examples:TestLib:app:core"))

    // Choose ONE native bridge — lib-core (JNI) or lib-ffm (FFM).
    // Both provide identical public APIs; only the internal native bridge differs.
    // implementation(project(":examples:TestLib:lib:lib-core"))    // JNI (default)
    implementation(project(":examples:TestLib:lib:lib-ffm")) // FFM (Java 22+, no JNI overhead)

    implementation(project(":examples:TestLib:lib:lib-desktop"))

    implementation("com.badlogicgames.gdx:gdx-platform:${LibExt.gdxVersion}:natives-desktop")
    implementation("com.badlogicgames.gdx:gdx-backend-lwjgl3:${LibExt.gdxVersion}")

    // JNI bridge for comparison benchmarks (not on default classpath)
    jniBridge(project(":examples:TestLib:lib:lib-core"))
}

tasks.register<JavaExec>("TestLib_run_app_desktop") {
    group = "example-desktop"
    description = "Run desktop app"
    mainClass.set("com.github.xpenatan.jParser.example.app.Main")
    classpath = sourceSets["main"].runtimeClasspath

    // FFM requires JDK 22+ and native access
    javaLauncher.set(javaToolchains.launcherFor {
        languageVersion.set(JavaLanguageVersion.of(25))
    })
    jvmArgs("--enable-native-access=ALL-UNNAMED")

    if(DefaultNativePlatform.getCurrentOperatingSystem().isMacOsX) {
        jvmArgs("-XstartOnFirstThread")
    }
}

tasks.register<JavaExec>("TestLib_run_benchmark_desktop") {
    group = "example-desktop"
    description = "Run desktop app"
    mainClass.set("com.github.xpenatan.jParser.example.app.BenchmarkMain")
    classpath = sourceSets["main"].runtimeClasspath

    if(DefaultNativePlatform.getCurrentOperatingSystem().isMacOsX) {
        jvmArgs("-XstartOnFirstThread")
    }
}

tasks.register<JavaExec>("TestLib_run_native_benchmark_desktop") {
    group = "example-desktop"
    description = "Run native bridge benchmark (JNI vs FFM)"
    mainClass.set("com.github.xpenatan.jParser.example.app.NativeBridgeBenchmarkMain")
    classpath = sourceSets["main"].runtimeClasspath

    // FFM requires JDK 22+ and native access
    javaLauncher.set(javaToolchains.launcherFor {
        languageVersion.set(JavaLanguageVersion.of(25))
    })
    jvmArgs("--enable-native-access=ALL-UNNAMED")

    if(DefaultNativePlatform.getCurrentOperatingSystem().isMacOsX) {
        jvmArgs("-XstartOnFirstThread")
    }
}

// ---------------------------------------------------------------------------
// JNI vs FFM comparison benchmark tasks
// ---------------------------------------------------------------------------

// Build a JNI classpath: take the default runtime classpath, remove lib-ffm
// artifacts, and add lib-core (JNI) from the jniBridge configuration.
// The JNI native DLL directories are prepended so the JNI-compiled DLLs are
// found by the loader *before* the FFM DLLs bundled inside the desktop JARs.
val testLibJniDllDir = file("${projectDir}/../../lib/lib-build/build/c++/libs/windows/vc")
val idlHelperJniDllDir = file("${rootProject.projectDir}/idl-helper/idl-helper-build/build/c++/libs/windows/vc")
val jniClasspath = files(testLibJniDllDir, idlHelperJniDllDir) + sourceSets["main"].runtimeClasspath.filter { file ->
    !file.absolutePath.replace('\\', '/').contains("lib-ffm") &&
    !file.absolutePath.replace('\\', '/').contains("idl-helper-ffm")
} + configurations["jniBridge"]

val benchmarkDir = layout.buildDirectory.dir("benchmark")

tasks.register<JavaExec>("TestLib_benchmark_jni") {
    group = "example-benchmark"
    description = "Run native bridge benchmark with JNI bridge, save CSV report"
    mainClass.set("com.github.xpenatan.jParser.example.app.NativeBridgeBenchmarkMain")
    classpath = jniClasspath
    systemProperty("benchmark.output", benchmarkDir.get().file("benchmark_jni.csv").asFile.absolutePath)

    if(DefaultNativePlatform.getCurrentOperatingSystem().isMacOsX) {
        jvmArgs("-XstartOnFirstThread")
    }

    doFirst {
        benchmarkDir.get().asFile.mkdirs()
    }
}

tasks.register<JavaExec>("TestLib_benchmark_ffm") {
    group = "example-benchmark"
    description = "Run native bridge benchmark with FFM bridge, save CSV report"
    mainClass.set("com.github.xpenatan.jParser.example.app.NativeBridgeBenchmarkMain")
    classpath = sourceSets["main"].runtimeClasspath
    systemProperty("benchmark.output", benchmarkDir.get().file("benchmark_ffm.csv").asFile.absolutePath)

    // FFM requires JDK 22+ and native access
    javaLauncher.set(javaToolchains.launcherFor {
        languageVersion.set(JavaLanguageVersion.of(25))
    })
    jvmArgs("--enable-native-access=ALL-UNNAMED")

    if(DefaultNativePlatform.getCurrentOperatingSystem().isMacOsX) {
        jvmArgs("-XstartOnFirstThread")
    }

    doFirst {
        benchmarkDir.get().asFile.mkdirs()
    }
}

tasks.register<JavaExec>("TestLib_benchmark_compare") {
    group = "example-benchmark"
    description = "Run JNI & FFM benchmarks then print a comparison table"
    dependsOn("TestLib_benchmark_jni", "TestLib_benchmark_ffm")

    mainClass.set("com.github.xpenatan.jParser.example.app.NativeBridgeBenchmarkCompare")
    classpath = sourceSets["main"].runtimeClasspath
    args(
        benchmarkDir.get().file("benchmark_jni.csv").asFile.absolutePath,
        benchmarkDir.get().file("benchmark_ffm.csv").asFile.absolutePath,
        benchmarkDir.get().file("benchmark_compare.txt").asFile.absolutePath
    )
}

// Ensure JNI runs before FFM when both are requested
tasks.named("TestLib_benchmark_ffm") { mustRunAfter("TestLib_benchmark_jni") }

// ---------------------------------------------------------------------------
// JNI vs FFM FPS benchmark tasks
// ---------------------------------------------------------------------------

val fpsBenchmarkDir = layout.buildDirectory.dir("benchmark")

tasks.register<JavaExec>("TestLib_fps_benchmark_jni") {
    group = "example-benchmark"
    description = "Run FPS benchmark with JNI bridge, save CSV report"
    mainClass.set("com.github.xpenatan.jParser.example.app.NativeBridgeFpsBenchmarkMain")
    classpath = jniClasspath
    systemProperty("benchmark.fps.output", fpsBenchmarkDir.get().file("fps_benchmark_jni.csv").asFile.absolutePath)

    if(DefaultNativePlatform.getCurrentOperatingSystem().isMacOsX) {
        jvmArgs("-XstartOnFirstThread")
    }

    doFirst {
        fpsBenchmarkDir.get().asFile.mkdirs()
    }
}

tasks.register<JavaExec>("TestLib_fps_benchmark_ffm") {
    group = "example-benchmark"
    description = "Run FPS benchmark with FFM bridge, save CSV report"
    mainClass.set("com.github.xpenatan.jParser.example.app.NativeBridgeFpsBenchmarkMain")
    classpath = sourceSets["main"].runtimeClasspath
    systemProperty("benchmark.fps.output", fpsBenchmarkDir.get().file("fps_benchmark_ffm.csv").asFile.absolutePath)

    javaLauncher.set(javaToolchains.launcherFor {
        languageVersion.set(JavaLanguageVersion.of(25))
    })
    jvmArgs("--enable-native-access=ALL-UNNAMED")

    if(DefaultNativePlatform.getCurrentOperatingSystem().isMacOsX) {
        jvmArgs("-XstartOnFirstThread")
    }

    doFirst {
        fpsBenchmarkDir.get().asFile.mkdirs()
    }
}

tasks.register<JavaExec>("TestLib_fps_benchmark_compare") {
    group = "example-benchmark"
    description = "Run JNI & FFM FPS benchmarks then print a comparison table"
    dependsOn("TestLib_fps_benchmark_jni", "TestLib_fps_benchmark_ffm")

    mainClass.set("com.github.xpenatan.jParser.example.app.NativeBridgeFpsBenchmarkCompare")
    classpath = sourceSets["main"].runtimeClasspath
    args(
        fpsBenchmarkDir.get().file("fps_benchmark_jni.csv").asFile.absolutePath,
        fpsBenchmarkDir.get().file("fps_benchmark_ffm.csv").asFile.absolutePath,
        fpsBenchmarkDir.get().file("fps_benchmark_compare.txt").asFile.absolutePath
    )
}

// Ensure JNI runs before FFM when both are requested
tasks.named("TestLib_fps_benchmark_ffm") { mustRunAfter("TestLib_fps_benchmark_jni") }

