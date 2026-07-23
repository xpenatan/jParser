plugins {
    id("java")
}

dependencies {
    implementation(project(":jParser:runtime:desktop:runtime-desktop-ffm"))
    testImplementation(libs.junit)
}

java {
    sourceCompatibility = JavaVersion.toVersion(libs.versions.javaFfm.get())
    targetCompatibility = JavaVersion.toVersion(libs.versions.javaFfm.get())
}

tasks.register<JavaExec>("perf_smoke") {
    group = "benchmark"
    description = "Run lightweight performance smoke benchmarks for CI checks"
    mainClass.set("com.github.xpenatan.jparser.benchmark.PerfSmokeMain")
    classpath = sourceSets["main"].runtimeClasspath
    jvmArgs("--enable-native-access=ALL-UNNAMED")
}
