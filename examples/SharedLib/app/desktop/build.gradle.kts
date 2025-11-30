import org.gradle.nativeplatform.platform.internal.DefaultNativePlatform

plugins {
    id("java")
}

java {
    sourceCompatibility = JavaVersion.toVersion(LibExt.java8Target)
    targetCompatibility = JavaVersion.toVersion(LibExt.java8Target)
}

dependencies {
    implementation(project(":examples:SharedLib:app:core"))
    implementation(project(":examples:SharedLib:libA:lib-desktop"))
    implementation(project(":examples:SharedLib:libB:lib-desktop"))
    implementation(project(":idl-helper:idl-helper-desktop"))

    implementation("com.badlogicgames.gdx:gdx-platform:${LibExt.gdxVersion}:natives-desktop")
    implementation("com.badlogicgames.gdx:gdx-backend-lwjgl3:${LibExt.gdxVersion}")
}

tasks.register<JavaExec>("SharedLib_run_app_desktop") {
    group = "example-desktop"
    description = "Run desktop app"
    mainClass.set("com.github.xpenatan.jParser.example.app.Main")
    classpath = sourceSets["main"].runtimeClasspath

    if(DefaultNativePlatform.getCurrentOperatingSystem().isMacOsX) {
        jvmArgs("-XstartOnFirstThread")
    }
}