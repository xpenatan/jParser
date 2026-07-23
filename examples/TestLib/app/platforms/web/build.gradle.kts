plugins {
    id("java")
}

java {
    sourceCompatibility = JavaVersion.toVersion(libs.versions.javaWeb.get())
    targetCompatibility = JavaVersion.toVersion(libs.versions.javaWeb.get())
}

dependencies {
    implementation(project(":examples:TestLib:app:core"))
    implementation(project(":examples:TestLib:lib:web:TestLib-web"))

    implementation(libs.gdxCore)
    implementation(libs.gdxTeavmBackendWeb)
}

tasks.register<JavaExec>("TestLib_run_app_web") {
    group = "example-web"
    description = "Build web app"
    dependsOn(
        ":jParser:runtime:builder:runtime_helper_build_project_web_wasm",
        ":examples:TestLib:lib:builder:TestLib_build_project_web_wasm"
    )
    mainClass.set("Build")
    classpath = sourceSets["main"].runtimeClasspath
    systemProperty("jparser.web.startJetty", System.getProperty("jparser.web.startJetty", "true"))
}

tasks.register<JavaExec>("TestLib_run_benchmark_web") {
    group = "example-web"
    description = "Build web benchmark"
    dependsOn(
        ":jParser:runtime:builder:runtime_helper_build_project_web_wasm",
        ":examples:TestLib:lib:builder:TestLib_build_project_web_wasm"
    )
    mainClass.set("BenchmarkBuild")
    classpath = sourceSets["main"].runtimeClasspath
    systemProperty("jparser.web.startJetty", System.getProperty("jparser.web.startJetty", "true"))
}
