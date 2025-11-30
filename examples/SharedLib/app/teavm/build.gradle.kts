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
    implementation(project(":examples:SharedLib:app:core"))
    implementation(project(":examples:SharedLib:libA:lib-teavm"))
    implementation(project(":examples:SharedLib:libB:lib-teavm"))
    implementation(project(":idl-helper:idl-helper-teavm"))

    implementation("com.badlogicgames.gdx:gdx:${LibExt.gdxVersion}")
    implementation("com.github.xpenatan.gdx-teavm:backend-teavm:${LibExt.gdxTeaVMVersion}")
}

tasks.register<JavaExec>("SharedLib_build_app_teavm") {
    group = "example-teavm"
    description = "Build teavm app"
    mainClass.set("Build")
    classpath = sourceSets["main"].runtimeClasspath
}

tasks.register("SharedLib_run_app_teavm") {
    group = "example-teavm"
    description = "Run teavm app"
    val list = listOf("SharedLib_build_app_teavm", "jettyRun")
    dependsOn(list)

    tasks.findByName("jettyRun")?.mustRunAfter("SharedLib_build_app_teavm")
}