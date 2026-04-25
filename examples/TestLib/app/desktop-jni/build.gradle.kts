import org.gradle.nativeplatform.platform.internal.DefaultNativePlatform

plugins {
    id("java")
}

// Expose the shared core test sources so the same headless test can be executed for both
// JNI and FFM app modules without duplicating test code.
sourceSets["test"].java.srcDir(rootProject.file("examples/TestLib/app/core/src/test/java"))

// Configure headless tests for JNI module
tasks.test {
    useJUnit()
    systemProperty("java.awt.headless", "true")
    // Ensure JNI native artifacts are built before running tests
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
    runtimeOnly(project(":examples:TestLib:lib:lib-desktop-jni"))

    // test-time dependencies required to initialize native loaders
    testImplementation("junit:junit:${LibExt.jUnitVersion}")
    testRuntimeOnly(project(":examples:TestLib:lib:lib-desktop-jni"))
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