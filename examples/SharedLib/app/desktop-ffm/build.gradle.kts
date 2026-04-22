import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.nativeplatform.platform.internal.DefaultNativePlatform

plugins {
    id("java")
}

java {
    sourceCompatibility = JavaVersion.toVersion(24)
    targetCompatibility = JavaVersion.toVersion(24)
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(24))
    }
}

val isMacOs = DefaultNativePlatform.getCurrentOperatingSystem().isMacOsX

dependencies {
    implementation(project(":examples:SharedLib:app:core"))

    implementation("com.badlogicgames.gdx:gdx-platform:${LibExt.gdxVersion}:natives-desktop")
    implementation("com.badlogicgames.gdx:gdx-backend-lwjgl3:${LibExt.gdxVersion}")

    implementation(project(":examples:SharedLib:libA:lib-desktop-ffm"))
    implementation(project(":examples:SharedLib:libB:lib-desktop-ffm"))
}

tasks.register<JavaExec>("SharedLib_run_app_ffm_desktop") {
    group = "example-desktop"
    description = "Run desktop app with FFM bridge"
    mainClass.set("com.github.xpenatan.jParser.example.app.Main")
    classpath = sourceSets["main"].runtimeClasspath
    javaLauncher.set(javaToolchains.launcherFor {
        languageVersion.set(JavaLanguageVersion.of(24))
    })
    jvmArgs("--enable-native-access=ALL-UNNAMED")
    if(isMacOs) {
        jvmArgs("-XstartOnFirstThread")
    }
}