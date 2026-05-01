plugins {
    id("java")
}

java {
    sourceCompatibility = JavaVersion.toVersion(LibExt.javaWebTarget)
    targetCompatibility = JavaVersion.toVersion(LibExt.javaWebTarget)
}

dependencies {
    implementation(project(":examples:TestLib:app:core"))
    implementation(project(":examples:TestLib:lib:lib-web"))

    implementation("com.badlogicgames.gdx:gdx:${LibExt.gdxVersion}")
    implementation("com.github.xpenatan.gdx-teavm:backend-web:${LibExt.gdxTeaVMVersion}")
}

tasks.register<JavaExec>("TestLib_run_app_web") {
    group = "example-teavm"
    description = "Build teavm app"
    mainClass.set("Build")
    classpath = sourceSets["main"].runtimeClasspath
}

tasks.register<JavaExec>("TestLib_run_benchmark_web") {
    group = "example-teavm"
    description = "Build teavm benchmark"
    mainClass.set("BenchmarkBuild")
    classpath = sourceSets["main"].runtimeClasspath
}