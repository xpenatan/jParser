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
    dependsOn(
        ":jParser:runtime:plugin:jParser_build_windows64_jni",
        ":examples:TestLib:lib:plugin:jParser_build_windows64_jni",
        ":examples:TestLib:lib:lib-jni:assemble"
    )
    // Print test standard output (System.out.println) to the console for CI logs
    testLogging {
        showStandardStreams = true
        events("passed", "skipped", "failed")
    }
    if(isMacOs) {
        jvmArgs("-XstartOnFirstThread")
    }
}

tasks.named("test") {
    outputs.upToDateWhen { false }
}

java {
    sourceCompatibility = JavaVersion.toVersion(LibExt.javaMainTarget)
    targetCompatibility = JavaVersion.toVersion(LibExt.javaMainTarget)
}

val benchmarkDir = rootProject.layout.buildDirectory.dir("testlib-benchmark")
val isMacOs = DefaultNativePlatform.getCurrentOperatingSystem().isMacOsX

dependencies {
    implementation(project(":examples:TestLib:app:core"))

    implementation("com.badlogicgames.gdx:gdx-platform:${LibExt.gdxVersion}:natives-desktop")
    implementation("com.badlogicgames.gdx:gdx-backend-lwjgl3:${LibExt.gdxVersion}")

    implementation(project(":examples:TestLib:lib:lib-jni"))

    implementation(project(":jParser:runtime:runtime-jvm:jni"))

    testImplementation("junit:junit:${LibExt.jUnitVersion}")
}

tasks.register<JavaExec>("TestLib_run_app_desktop_jni") {
    group = "example-desktop"
    description = "Run desktop app with JNI bridge"
    dependsOn(
        ":jParser:runtime:plugin:jParser_build_windows64_jni",
        ":examples:TestLib:lib:plugin:jParser_build_windows64_jni"
    )
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
