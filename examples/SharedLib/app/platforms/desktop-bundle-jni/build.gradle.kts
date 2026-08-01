import org.gradle.nativeplatform.platform.internal.DefaultNativePlatform

plugins {
    java
}

java {
    sourceCompatibility = JavaVersion.toVersion(libs.versions.javaMain.get())
    targetCompatibility = JavaVersion.toVersion(libs.versions.javaMain.get())
}

val isMacOs = DefaultNativePlatform.getCurrentOperatingSystem().isMacOsX

dependencies {
    implementation(project(":examples:SharedLib:app:core"))

    implementation(variantOf(libs.gdxPlatform) { classifier("natives-desktop") })
    implementation(libs.gdxBackendLwjgl3)

    // The bundle project provides both generated bindings, the JNI runtime,
    // and the one fat DLL/SO/DYLIB through its ordinary runtime JAR.
    implementation(project(":examples:SharedLib:bundle"))
}

tasks.register<JavaExec>("SharedLib_run_app_desktop_bundle_jni") {
    group = "example-desktop"
    description = "Run the desktop app with the SharedLib JNI fat native bundle."
    mainClass.set("com.github.xpenatan.jParser.example.app.Main")
    classpath = sourceSets["main"].runtimeClasspath
    if(isMacOs) {
        jvmArgs("-XstartOnFirstThread")
    }
}
