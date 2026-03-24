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

    // Choose ONE native bridge per lib — lib-core (JNI) or lib-ffm (FFM).
    // implementation(project(":examples:SharedLib:libA:lib-core"))    // JNI (default)
    // implementation(project(":examples:SharedLib:libB:lib-core"))    // JNI (default)
    implementation(project(":examples:SharedLib:libA:lib-ffm")) // FFM (Java 22+, no JNI overhead)
    implementation(project(":examples:SharedLib:libB:lib-ffm")) // FFM (Java 22+, no JNI overhead)

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

    // FFM requires JDK 22+ and native access
    javaLauncher.set(javaToolchains.launcherFor {
        languageVersion.set(JavaLanguageVersion.of(25))
    })
    jvmArgs("--enable-native-access=ALL-UNNAMED")

    if(DefaultNativePlatform.getCurrentOperatingSystem().isMacOsX) {
        jvmArgs("-XstartOnFirstThread")
    }
}