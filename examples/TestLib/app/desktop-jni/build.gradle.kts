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
    implementation(project(":examples:TestLib:lib:lib-core"))
    implementation(project(":examples:TestLib:lib:lib-desktop-jni"))
    implementation(project(":idl-helper:idl-helper-desktop-jni"))

    implementation("com.badlogicgames.gdx:gdx-platform:${LibExt.gdxVersion}:natives-desktop")
    implementation("com.badlogicgames.gdx:gdx-backend-lwjgl3:${LibExt.gdxVersion}")
}

tasks.register<JavaExec>("TestLib_run_app_desktop") {
    group = "example-desktop"
    description = "Run desktop app (JNI)"
    mainClass.set("com.github.xpenatan.jParser.example.app.Main")
    classpath = sourceSets["main"].runtimeClasspath

    if(DefaultNativePlatform.getCurrentOperatingSystem().isMacOsX) {
        jvmArgs("-XstartOnFirstThread")
    }
}

tasks.register<JavaExec>("TestLib_run_benchmark_desktop") {
    group = "example-desktop"
    description = "Run enum benchmark (JNI)"
    mainClass.set("com.github.xpenatan.jParser.example.app.BenchmarkMain")
    classpath = sourceSets["main"].runtimeClasspath

    if(DefaultNativePlatform.getCurrentOperatingSystem().isMacOsX) {
        jvmArgs("-XstartOnFirstThread")
    }
}

// ---------------------------------------------------------------------------
// JNI benchmark tasks
// ---------------------------------------------------------------------------

val benchmarkDir = layout.buildDirectory.dir("benchmark")

tasks.register<JavaExec>("TestLib_benchmark_jni") {
    group = "example-benchmark"
    description = "Run native bridge benchmark with JNI bridge, save CSV report"
    mainClass.set("com.github.xpenatan.jParser.example.app.NativeBridgeBenchmarkMain")
    classpath = sourceSets["main"].runtimeClasspath
    systemProperty("benchmark.output", benchmarkDir.get().file("benchmark_jni.csv").asFile.absolutePath)

    if(DefaultNativePlatform.getCurrentOperatingSystem().isMacOsX) {
        jvmArgs("-XstartOnFirstThread")
    }

    doFirst {
        benchmarkDir.get().asFile.mkdirs()
    }
}

tasks.register<JavaExec>("TestLib_fps_benchmark_jni") {
    group = "example-benchmark"
    description = "Run FPS benchmark with JNI bridge, save CSV report"
    mainClass.set("com.github.xpenatan.jParser.example.app.NativeBridgeFpsBenchmarkMain")
    classpath = sourceSets["main"].runtimeClasspath
    systemProperty("benchmark.fps.output", benchmarkDir.get().file("fps_benchmark_jni.csv").asFile.absolutePath)

    if(DefaultNativePlatform.getCurrentOperatingSystem().isMacOsX) {
        jvmArgs("-XstartOnFirstThread")
    }

    doFirst {
        benchmarkDir.get().asFile.mkdirs()
    }
}

