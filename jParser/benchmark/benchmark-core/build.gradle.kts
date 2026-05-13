plugins {
    id("java")
}

dependencies {
    implementation(project(":jParser:runtime:runtime-ffm"))
    testImplementation("junit:junit:${LibExt.jUnitVersion}")
}

java {
    sourceCompatibility = JavaVersion.toVersion(LibExt.javaFFMTarget)
    targetCompatibility = JavaVersion.toVersion(LibExt.javaFFMTarget)
}

tasks.register<JavaExec>("perf_smoke") {
    group = "benchmark"
    description = "Run lightweight performance smoke benchmarks for CI checks"
    mainClass.set("com.github.xpenatan.jparser.benchmark.PerfSmokeMain")
    classpath = sourceSets["main"].runtimeClasspath
    jvmArgs("--enable-native-access=ALL-UNNAMED")
}

