import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.nativeplatform.platform.internal.DefaultNativePlatform

plugins {
    id("java")
}

java {
    sourceCompatibility = JavaVersion.toVersion(LibExt.javaFFMTarget)
    targetCompatibility = JavaVersion.toVersion(LibExt.javaFFMTarget)
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(LibExt.javaFFMTarget))
    }
}

val isMacOs = DefaultNativePlatform.getCurrentOperatingSystem().isMacOsX

dependencies {
    implementation(project(":examples:SharedLib:app:core"))

    implementation("com.badlogicgames.gdx:gdx-platform:${LibExt.gdxVersion}:natives-desktop")
    implementation("com.badlogicgames.gdx:gdx-backend-lwjgl3:${LibExt.gdxVersion}")

    implementation(project(":examples:SharedLib:libA:lib-desktop-ffm"))
    implementation(project(":examples:SharedLib:libB:lib-desktop-ffm"))
    // bring in platform native jars produced by idl-helper
    runtimeOnly(project(mapOf("path" to ":idl-helper:idl-helper-desktop-ffm", "configuration" to "nativeRuntime")))
    testRuntimeOnly(project(mapOf("path" to ":idl-helper:idl-helper-desktop-ffm", "configuration" to "nativeRuntime")))
}

tasks.register<JavaExec>("SharedLib_run_app_desktop_ffm") {
    group = "example-desktop"
    description = "Run desktop app with FFM bridge"
    mainClass.set("com.github.xpenatan.jParser.example.app.Main")
    classpath = sourceSets["main"].runtimeClasspath
    javaLauncher.set(javaToolchains.launcherFor {
        languageVersion.set(JavaLanguageVersion.of(LibExt.javaFFMTarget))
    })
    jvmArgs("--enable-native-access=ALL-UNNAMED")
    if(isMacOs) {
        jvmArgs("-XstartOnFirstThread")
    }
}