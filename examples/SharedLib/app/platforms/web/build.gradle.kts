plugins {
    id("java")
}

java {
    sourceCompatibility = JavaVersion.toVersion(libs.versions.javaWeb.get())
    targetCompatibility = JavaVersion.toVersion(libs.versions.javaWeb.get())
}

dependencies {
    implementation(project(":examples:SharedLib:app:core"))
    implementation(project(":examples:SharedLib:libA:web:LibA-web"))
    implementation(project(":examples:SharedLib:libB:web:LibB-web"))

    implementation(libs.gdxCore)
    implementation(libs.gdxTeavmBackendWeb)
}

tasks.register<JavaExec>("SharedLib_run_app_web") {
    group = "example-web"
    description = "Build web app"
    dependsOn(
        ":jParser:runtime:builder:runtime_helper_build_project_web_wasm",
        ":examples:SharedLib:libA:builder:LibA_build_project_web_wasm",
        ":examples:SharedLib:libB:builder:LibB_build_project_web_wasm"
    )
    mainClass.set("Build")
    classpath = sourceSets["main"].runtimeClasspath
    systemProperty("jparser.web.startJetty", System.getProperty("jparser.web.startJetty", "true"))
}
