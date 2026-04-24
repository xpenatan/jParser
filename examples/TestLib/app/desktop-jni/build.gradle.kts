import org.gradle.nativeplatform.platform.internal.DefaultNativePlatform

plugins {
    id("java")
}

java {
    sourceCompatibility = JavaVersion.toVersion(LibExt.java8Target)
    targetCompatibility = JavaVersion.toVersion(LibExt.java8Target)
}

val benchmarkDir = rootProject.layout.buildDirectory.dir("testlib-benchmark")
val isMacOs = DefaultNativePlatform.getCurrentOperatingSystem().isMacOsX

dependencies {
    implementation(project(":examples:TestLib:app:core"))

    implementation("com.badlogicgames.gdx:gdx-platform:${LibExt.gdxVersion}:natives-desktop")
    implementation("com.badlogicgames.gdx:gdx-backend-lwjgl3:${LibExt.gdxVersion}")

    implementation(project(":examples:TestLib:lib:lib-desktop-jni"))
}

tasks.register<JavaExec>("TestLib_run_app_desktop_jni") {
    group = "example-desktop"
    description = "Run desktop app with JNI bridge"
    mainClass.set("com.github.xpenatan.jParser.example.app.Main")
    classpath = sourceSets["main"].runtimeClasspath
    if(isMacOs) {
        jvmArgs("-XstartOnFirstThread")
    }
}

tasks.register<JavaExec>("TestLib_enum_benchmark_jni") {
    group = "example-benchmark"
    description = "Run enum benchmark with JNI bridge and write CSV output"
    mainClass.set("com.github.xpenatan.jParser.example.app.BenchmarkMain")
    systemProperty("benchmark.enum.output", benchmarkDir.get().file("enum_benchmark_jni.csv").asFile.absolutePath)
    classpath = sourceSets["main"].runtimeClasspath
    if(isMacOs) {
        jvmArgs("-XstartOnFirstThread")
    }

    doFirst {
        benchmarkDir.get().asFile.mkdirs()
    }
}

tasks.register<JavaExec>("TestLib_throughput_benchmark_jni") {
    group = "example-benchmark"
    description = "Run native bridge throughput benchmark with JNI bridge"
    mainClass.set("com.github.xpenatan.jParser.example.app.NativeBridgeBenchmarkMain")
    systemProperty("benchmark.output", benchmarkDir.get().file("benchmark_jni.csv").asFile.absolutePath)
    classpath = sourceSets["main"].runtimeClasspath
    if(isMacOs) {
        jvmArgs("-XstartOnFirstThread")
    }

    doFirst {
        benchmarkDir.get().asFile.mkdirs()
    }
}

tasks.register<JavaExec>("TestLib_fps_benchmark_jni") {
    group = "example-benchmark"
    description = "Run FPS benchmark with JNI bridge"
    mainClass.set("com.github.xpenatan.jParser.example.app.NativeBridgeFpsBenchmarkMain")
    systemProperty("benchmark.fps.output", benchmarkDir.get().file("fps_benchmark_jni.csv").asFile.absolutePath)
    classpath = sourceSets["main"].runtimeClasspath
    if(isMacOs) {
        jvmArgs("-XstartOnFirstThread")
    }

    doFirst {
        benchmarkDir.get().asFile.mkdirs()
    }
}

tasks.register<JavaExec>("TestLib_fps_benchmark_interactive_jni") {
    group = "example-benchmark"
    description = "Run FPS benchmark with JNI bridge in interactive mode"
    mainClass.set("com.github.xpenatan.jParser.example.app.NativeBridgeFpsBenchmarkMain")
    systemProperty("benchmark.fps.mode", "interactive")
    classpath = sourceSets["main"].runtimeClasspath
    if(isMacOs) {
        jvmArgs("-XstartOnFirstThread")
    }
}