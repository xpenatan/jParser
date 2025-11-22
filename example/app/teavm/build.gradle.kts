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
    implementation(project(":example:app:core"))
    implementation(project(":example:lib:lib-teavm"))

    implementation("com.badlogicgames.gdx:gdx:${LibExt.gdxVersion}")
    implementation("com.github.xpenatan.gdx-teavm:backend-teavm:${LibExt.gdxTeaVMVersion}")
}

tasks.register<JavaExec>("build-app-teavm") {
    group = "example-teavm"
    description = "Build teavm app"
    mainClass.set("Build")
    classpath = sourceSets["main"].runtimeClasspath
}

tasks.register("run-app-teavm") {
    group = "example-teavm"
    description = "Run teavm app"
    val list = listOf("build-app-teavm", "jettyRun")
    dependsOn(list)

    tasks.findByName("jettyRun")?.mustRunAfter("build-app-teavm")
}

tasks.register<JavaExec>("build-benchmark-teavm") {
    group = "example-teavm"
    description = "Build teavm benchmark"
    mainClass.set("BenchmarkBuild")
    classpath = sourceSets["main"].runtimeClasspath
}

tasks.register("run-benchmark-teavm") {
    group = "example-teavm"
    description = "Run teavm benchmark"
    val list = listOf("build-benchmark-teavm", "jettyRun")
    dependsOn(list)

    tasks.findByName("jettyRun")?.mustRunAfter("build-benchmark-teavm")
}