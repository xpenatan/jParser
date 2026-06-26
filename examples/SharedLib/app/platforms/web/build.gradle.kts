plugins {
    id("java")
}

java {
    sourceCompatibility = JavaVersion.toVersion(LibExt.javaWebTarget)
    targetCompatibility = JavaVersion.toVersion(LibExt.javaWebTarget)
}

dependencies {
    implementation(project(":examples:SharedLib:app:core"))
    implementation(project(":examples:SharedLib:libA:lib-web"))
    implementation(project(":examples:SharedLib:libB:lib-web"))

    implementation("com.badlogicgames.gdx:gdx:${LibExt.gdxVersion}")
    implementation("com.github.xpenatan.gdx-teavm:backend-web:${LibExt.gdxTeaVMVersion}")
}

tasks.register<JavaExec>("SharedLib_run_app_web") {
    group = "example-web"
    description = "Build web app"
    dependsOn(
        ":jParser:runtime:runtime-build:runtime_helper_build_project_web_wasm",
        ":examples:SharedLib:libA:lib-build:LibA_build_project_web_wasm",
        ":examples:SharedLib:libB:lib-build:LibB_build_project_web_wasm"
    )
    mainClass.set("Build")
    classpath = sourceSets["main"].runtimeClasspath
    systemProperty("jparser.web.startJetty", System.getProperty("jparser.web.startJetty", "true"))
}
