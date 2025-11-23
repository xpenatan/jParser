plugins {
    id("java")
    id("org.gretty") version("4.1.10")
}


project.extra["webAppDir"] = File(projectDir, "build/dist/webapp")
gretty {
    contextPath = "/"
}

java {
    sourceCompatibility = JavaVersion.toVersion(LibExt.java11Target)
    targetCompatibility = JavaVersion.toVersion(LibExt.java11Target)
}

dependencies {
    implementation(project(":examples:TestLib:app:core"))
    implementation(project(":examples:TestLib:lib:lib-teavm"))

    implementation("com.badlogicgames.gdx:gdx:${LibExt.gdxVersion}")
    implementation("com.github.xpenatan.gdx-teavm:backend-teavm:${LibExt.gdxTeaVMVersion}")
}

tasks.register<JavaExec>("TestLib_build_app_teavm") {
    group = "example-teavm"
    description = "Build teavm app"
    mainClass.set("Build")
    classpath = sourceSets["main"].runtimeClasspath
}

tasks.register("TestLib_run_app_teavm") {
    group = "example-teavm"
    description = "Run teavm app"
    val list = listOf("TestLib_build_app_teavm", "jettyRun")
    dependsOn(list)

    tasks.findByName("jettyRun")?.mustRunAfter("TestLib_build_app_teavm")
}

tasks.register<JavaExec>("TestLib_build_benchmark_teavm") {
    group = "example-teavm"
    description = "Build teavm benchmark"
    mainClass.set("BenchmarkBuild")
    classpath = sourceSets["main"].runtimeClasspath
}

tasks.register("TestLib_run_benchmark_teavm") {
    group = "example-teavm"
    description = "Run teavm benchmark"
    val list = listOf("TestLib_build_benchmark_teavm", "jettyRun")
    dependsOn(list)

    tasks.findByName("jettyRun")?.mustRunAfter("TestLib_build_benchmark_teavm")
}