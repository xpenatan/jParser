import org.gradle.nativeplatform.platform.internal.DefaultNativePlatform

plugins {
    id("java")
}

java {
    sourceCompatibility = JavaVersion.toVersion(LibExt.java8Target)
    targetCompatibility = JavaVersion.toVersion(LibExt.java8Target)
}

val isMacOs = DefaultNativePlatform.getCurrentOperatingSystem().isMacOsX

dependencies {
    implementation(project(":examples:SharedLib:app:core"))

    implementation("com.badlogicgames.gdx:gdx-platform:${LibExt.gdxVersion}:natives-desktop")
    implementation("com.badlogicgames.gdx:gdx-backend-lwjgl3:${LibExt.gdxVersion}")

    implementation(project(":examples:SharedLib:libA:lib-desktop-jni"))
    implementation(project(":examples:SharedLib:libB:lib-desktop-jni"))
    // bring in platform native jars produced by idl-helper
    runtimeOnly(project(mapOf("path" to ":idl-helper:idl-helper-desktop-jni", "configuration" to "nativeRuntime")))
    // for tests as well
    testRuntimeOnly(project(mapOf("path" to ":idl-helper:idl-helper-desktop-jni", "configuration" to "nativeRuntime")))
}

tasks.register<JavaExec>("SharedLib_run_app_desktop_jni") {
    group = "example-desktop"
    description = "Run desktop app with JNI bridge"
    mainClass.set("com.github.xpenatan.jParser.example.app.Main")
    classpath = sourceSets["main"].runtimeClasspath
    if(isMacOs) {
        jvmArgs("-XstartOnFirstThread")
    }
}

