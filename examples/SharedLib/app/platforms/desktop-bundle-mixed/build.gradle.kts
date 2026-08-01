import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.nativeplatform.platform.internal.DefaultNativePlatform

plugins {
    java
}

java {
    sourceCompatibility = JavaVersion.toVersion(libs.versions.javaFfm.get())
    targetCompatibility = JavaVersion.toVersion(libs.versions.javaFfm.get())
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(libs.versions.javaFfm.get()))
    }
}

val isMacOs = DefaultNativePlatform.getCurrentOperatingSystem().isMacOsX

dependencies {
    implementation(project(":examples:SharedLib:app:core"))

    implementation(variantOf(libs.gdxPlatform) { classifier("natives-desktop") })
    implementation(libs.gdxBackendLwjgl3)

    // The bundle project provides the JNI/FFM classes, the FFM runtime, and
    // the one fat DLL/SO/DYLIB through its ordinary runtime JAR.
    implementation(project(":examples:SharedLib:bundle-mixed"))
}

tasks.register<JavaExec>("SharedLib_run_app_desktop_bundle_mixed") {
    group = "example-desktop"
    description = "Run the desktop app with the SharedLib mixed JNI/FFM fat native bundle."
    mainClass.set("com.github.xpenatan.jParser.example.app.Main")
    classpath = sourceSets["main"].runtimeClasspath
    javaLauncher.set(javaToolchains.launcherFor {
        languageVersion.set(JavaLanguageVersion.of(libs.versions.javaFfm.get()))
    })
    jvmArgs("--enable-native-access=ALL-UNNAMED")
    if(isMacOs) {
        jvmArgs("-XstartOnFirstThread")
    }
}
