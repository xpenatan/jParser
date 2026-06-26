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
    group = "example-web"
    description = "Build web app"
    dependsOn(
        ":jParser:runtime:plugin:jParser_build_web_wasm",
        ":examples:TestLib:lib:plugin:jParser_build_web_wasm"
    )
    mainClass.set("Build")
    classpath = sourceSets["main"].runtimeClasspath
    systemProperty("jparser.web.startJetty", System.getProperty("jparser.web.startJetty", "true"))
}

tasks.register<JavaExec>("TestLib_run_benchmark_web") {
    group = "example-web"
    description = "Build web benchmark"
    dependsOn(
        ":jParser:runtime:plugin:jParser_build_web_wasm",
        ":examples:TestLib:lib:plugin:jParser_build_web_wasm"
    )
    mainClass.set("BenchmarkBuild")
    classpath = sourceSets["main"].runtimeClasspath
    systemProperty("jparser.web.startJetty", System.getProperty("jparser.web.startJetty", "true"))
}
