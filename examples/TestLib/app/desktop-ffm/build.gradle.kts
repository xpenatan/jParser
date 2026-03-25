import org.gradle.nativeplatform.platform.internal.DefaultNativePlatform

plugins {
    id("java")
}

java {
    sourceCompatibility = JavaVersion.toVersion(LibExt.java8Target)
    targetCompatibility = JavaVersion.toVersion(LibExt.java8Target)
}

dependencies {
    implementation(project(":examples:TestLib:app:core"))
    implementation(project(":examples:TestLib:lib:lib-desktop-ffm"))

    implementation("com.badlogicgames.gdx:gdx-platform:${LibExt.gdxVersion}:natives-desktop")
    implementation("com.badlogicgames.gdx:gdx-backend-lwjgl3:${LibExt.gdxVersion}")
}

tasks.register<JavaExec>("TestLib_run_app_desktop") {
    group = "example-desktop"
    description = "Run desktop app (FFM)"
    mainClass.set("com.github.xpenatan.jParser.example.app.Main")
    classpath = sourceSets["main"].runtimeClasspath

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
    description = "Run enum benchmark (FFM)"
    mainClass.set("com.github.xpenatan.jParser.example.app.BenchmarkMain")
    classpath = sourceSets["main"].runtimeClasspath

    javaLauncher.set(javaToolchains.launcherFor {
        languageVersion.set(JavaLanguageVersion.of(25))
    })
    jvmArgs("--enable-native-access=ALL-UNNAMED")

    if(DefaultNativePlatform.getCurrentOperatingSystem().isMacOsX) {
        jvmArgs("-XstartOnFirstThread")
    }
}

// ---------------------------------------------------------------------------
// FFM benchmark tasks
// ---------------------------------------------------------------------------

val benchmarkDir = layout.buildDirectory.dir("benchmark")

tasks.register<JavaExec>("TestLib_benchmark_ffm") {
    group = "example-benchmark"
    description = "Run native bridge benchmark with FFM bridge, save CSV report"
    mainClass.set("com.github.xpenatan.jParser.example.app.NativeBridgeBenchmarkMain")
    classpath = sourceSets["main"].runtimeClasspath
    systemProperty("benchmark.output", benchmarkDir.get().file("benchmark_ffm.csv").asFile.absolutePath)

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

tasks.register<JavaExec>("TestLib_fps_benchmark_ffm") {
    group = "example-benchmark"
    description = "Run FPS benchmark with FFM bridge, save CSV report"
    mainClass.set("com.github.xpenatan.jParser.example.app.NativeBridgeFpsBenchmarkMain")
    classpath = sourceSets["main"].runtimeClasspath
    systemProperty("benchmark.fps.output", benchmarkDir.get().file("fps_benchmark_ffm.csv").asFile.absolutePath)

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

// ---------------------------------------------------------------------------
// JNI vs FFM comparison benchmark tasks
// These depend on JNI benchmark tasks from the desktop-jni module.
// ---------------------------------------------------------------------------

val jniBenchmarkDir = project(":examples:TestLib:app:desktop-jni").layout.buildDirectory.dir("benchmark")

tasks.register<JavaExec>("TestLib_benchmark_compare") {
    group = "example-benchmark"
    description = "Run JNI & FFM benchmarks then print a comparison table"
    dependsOn(":examples:TestLib:app:desktop-jni:TestLib_benchmark_jni", "TestLib_benchmark_ffm")

    mainClass.set("com.github.xpenatan.jParser.example.app.NativeBridgeBenchmarkCompare")
    classpath = sourceSets["main"].runtimeClasspath
    args(
        jniBenchmarkDir.get().file("benchmark_jni.csv").asFile.absolutePath,
        benchmarkDir.get().file("benchmark_ffm.csv").asFile.absolutePath,
        benchmarkDir.get().file("benchmark_compare.txt").asFile.absolutePath
    )
}

tasks.register<JavaExec>("TestLib_fps_benchmark_compare") {
    group = "example-benchmark"
    description = "Run JNI & FFM FPS benchmarks then print a comparison table"
    dependsOn(":examples:TestLib:app:desktop-jni:TestLib_fps_benchmark_jni", "TestLib_fps_benchmark_ffm")

    mainClass.set("com.github.xpenatan.jParser.example.app.NativeBridgeFpsBenchmarkCompare")
    classpath = sourceSets["main"].runtimeClasspath
    args(
        jniBenchmarkDir.get().file("fps_benchmark_jni.csv").asFile.absolutePath,
        benchmarkDir.get().file("fps_benchmark_ffm.csv").asFile.absolutePath,
        benchmarkDir.get().file("fps_benchmark_compare.txt").asFile.absolutePath
    )
}

// Ensure JNI runs before FFM when both are requested
tasks.named("TestLib_benchmark_ffm") { mustRunAfter(":examples:TestLib:app:desktop-jni:TestLib_benchmark_jni") }
tasks.named("TestLib_fps_benchmark_ffm") { mustRunAfter(":examples:TestLib:app:desktop-jni:TestLib_fps_benchmark_jni") }
