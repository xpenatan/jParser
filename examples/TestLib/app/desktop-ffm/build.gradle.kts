import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.nativeplatform.platform.internal.DefaultNativePlatform

plugins {
    id("java")
}

sourceSets["test"].java.srcDir(rootProject.file("examples/TestLib/app/core/src/test/java"))

// Configure headless tests for JNI module
tasks.test {
    useJUnit()
    systemProperty("java.awt.headless", "true")
    // Ensure native artifacts are built before running tests
    dependsOn(":examples:TestLib:lib:lib-desktop-jni:assemble")
    // Print test standard output (System.out.println) to the console for CI logs
    testLogging {
        showStandardStreams = true
        events("passed", "skipped", "failed")
    }
    if(isMacOs) {
        jvmArgs("-XstartOnFirstThread")
    }
}

java {
    sourceCompatibility = JavaVersion.toVersion(LibExt.javaFFMTarget)
    targetCompatibility = JavaVersion.toVersion(LibExt.javaFFMTarget)
}

val benchmarkDir = rootProject.layout.buildDirectory.dir("testlib-benchmark")
val isMacOs = DefaultNativePlatform.getCurrentOperatingSystem().isMacOsX

dependencies {
    implementation(project(":examples:TestLib:app:core"))

    implementation("com.badlogicgames.gdx:gdx-platform:${LibExt.gdxVersion}:natives-desktop")
    implementation("com.badlogicgames.gdx:gdx-backend-lwjgl3:${LibExt.gdxVersion}")

    implementation(project(":examples:TestLib:lib:lib-desktop-ffm"))
    // Pull platform native jar (classifier jars) produced by the idl-helper's `nativeRuntime` configuration
    runtimeOnly(project(mapOf("path" to ":idl-helper:idl-helper-desktop-ffm", "configuration" to "nativeRuntime")))

    testImplementation("junit:junit:${LibExt.jUnitVersion}")
    testRuntimeOnly(project(mapOf("path" to ":idl-helper:idl-helper-desktop-ffm", "configuration" to "nativeRuntime")))
}

tasks.register<JavaExec>("TestLib_run_app_desktop_ffm") {
    group = "example-desktop"
    description = "Run desktop app with FFM bridge"
    mainClass.set("com.github.xpenatan.jParser.example.app.Main")
    classpath = sourceSets["main"].runtimeClasspath
    javaLauncher.set(javaToolchains.launcherFor {
        languageVersion.set(JavaLanguageVersion.of(LibExt.javaFFMTarget))
    })
    jvmArgs("--enable-native-access=ALL-UNNAMED")
    if(isMacOs) {
        jvmArgs("-XstartOnFirstThread")
    }
}

tasks.register<JavaExec>("TestLib_enum_benchmark_ffm") {
    group = "example-benchmark"
    description = "Run enum benchmark with FFM bridge and write CSV output"
    mainClass.set("com.github.xpenatan.jParser.example.app.BenchmarkMain")
    systemProperty("benchmark.enum.output", benchmarkDir.get().file("enum_benchmark_ffm.csv").asFile.absolutePath)
    classpath = sourceSets["main"].runtimeClasspath
    javaLauncher.set(javaToolchains.launcherFor {
        languageVersion.set(JavaLanguageVersion.of(LibExt.javaFFMTarget))
    })
    jvmArgs("--enable-native-access=ALL-UNNAMED")
    if(isMacOs) {
        jvmArgs("-XstartOnFirstThread")
    }

    doFirst {
        benchmarkDir.get().asFile.mkdirs()
    }
}

tasks.register<JavaExec>("TestLib_throughput_benchmark_ffm") {
    group = "example-benchmark"
    description = "Run native bridge throughput benchmark with FFM bridge"
    mainClass.set("com.github.xpenatan.jParser.example.app.NativeBridgeBenchmarkMain")
    systemProperty("benchmark.output", benchmarkDir.get().file("benchmark_ffm.csv").asFile.absolutePath)
    classpath = sourceSets["main"].runtimeClasspath
    javaLauncher.set(javaToolchains.launcherFor {
        languageVersion.set(JavaLanguageVersion.of(LibExt.javaFFMTarget))
    })
    jvmArgs("--enable-native-access=ALL-UNNAMED")
    if(isMacOs) {
        jvmArgs("-XstartOnFirstThread")
    }

    doFirst {
        benchmarkDir.get().asFile.mkdirs()
    }
}

tasks.register<JavaExec>("TestLib_fps_benchmark_ffm") {
    group = "example-benchmark"
    description = "Run FPS benchmark with FFM bridge"
    mainClass.set("com.github.xpenatan.jParser.example.app.NativeBridgeFpsBenchmarkMain")
    systemProperty("benchmark.fps.output", benchmarkDir.get().file("fps_benchmark_ffm.csv").asFile.absolutePath)
    classpath = sourceSets["main"].runtimeClasspath
    javaLauncher.set(javaToolchains.launcherFor {
        languageVersion.set(JavaLanguageVersion.of(LibExt.javaFFMTarget))
    })
    jvmArgs("--enable-native-access=ALL-UNNAMED")
    if(isMacOs) {
        jvmArgs("-XstartOnFirstThread")
    }

    doFirst {
        benchmarkDir.get().asFile.mkdirs()
    }
}

tasks.register<JavaExec>("TestLib_fps_benchmark_interactive_ffm") {
    group = "example-benchmark"
    description = "Run FPS benchmark with FFM bridge in interactive mode"
    mainClass.set("com.github.xpenatan.jParser.example.app.NativeBridgeFpsBenchmarkMain")
    systemProperty("benchmark.fps.mode", "interactive")
    classpath = sourceSets["main"].runtimeClasspath
    javaLauncher.set(javaToolchains.launcherFor {
        languageVersion.set(JavaLanguageVersion.of(LibExt.javaFFMTarget))
    })
    jvmArgs("--enable-native-access=ALL-UNNAMED")
    if(isMacOs) {
        jvmArgs("-XstartOnFirstThread")
    }
}

tasks.register<JavaExec>("TestLib_throughput_benchmark_compare") {
    group = "example-benchmark"
    description = "Run JNI & FFM benchmarks then print a comparison table"
    dependsOn(":examples:TestLib:app:desktop-jni:TestLib_throughput_benchmark_jni", "TestLib_throughput_benchmark_ffm")

    mainClass.set("com.github.xpenatan.jParser.example.app.NativeBridgeBenchmarkCompare")
    classpath = sourceSets["main"].runtimeClasspath
    args(
        benchmarkDir.get().file("benchmark_jni.csv").asFile.absolutePath,
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
        benchmarkDir.get().file("fps_benchmark_jni.csv").asFile.absolutePath,
        benchmarkDir.get().file("fps_benchmark_ffm.csv").asFile.absolutePath,
        benchmarkDir.get().file("fps_benchmark_compare.txt").asFile.absolutePath
    )
}

tasks.register<JavaExec>("TestLib_enum_benchmark_compare") {
    group = "example-benchmark"
    description = "Run JNI & FFM enum benchmarks then print a comparison table"
    dependsOn(":examples:TestLib:app:desktop-jni:TestLib_enum_benchmark_jni", "TestLib_enum_benchmark_ffm")

    mainClass.set("com.github.xpenatan.jParser.example.app.EnumBenchmarkCompare")
    classpath = sourceSets["main"].runtimeClasspath
    args(
        benchmarkDir.get().file("enum_benchmark_jni.csv").asFile.absolutePath,
        benchmarkDir.get().file("enum_benchmark_ffm.csv").asFile.absolutePath,
        benchmarkDir.get().file("enum_benchmark_compare.txt").asFile.absolutePath
    )
}

tasks.named("TestLib_throughput_benchmark_ffm") {
    mustRunAfter(":examples:TestLib:app:desktop-jni:TestLib_throughput_benchmark_jni")
}

tasks.named("TestLib_fps_benchmark_ffm") {
    mustRunAfter(":examples:TestLib:app:desktop-jni:TestLib_fps_benchmark_jni")
}

tasks.named("TestLib_enum_benchmark_ffm") {
    mustRunAfter(":examples:TestLib:app:desktop-jni:TestLib_enum_benchmark_jni")
}

