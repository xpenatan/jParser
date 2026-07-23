import org.gradle.nativeplatform.platform.internal.DefaultNativePlatform

plugins {
    id("java")
}

sourceSets["test"].java.srcDir(rootProject.file("examples/SharedLib/app/core/src/test/java"))

java {
    sourceCompatibility = JavaVersion.toVersion(libs.versions.javaMain.get())
    targetCompatibility = JavaVersion.toVersion(libs.versions.javaMain.get())
}

val isMacOs = DefaultNativePlatform.getCurrentOperatingSystem().isMacOsX
val runtimeJniBuildTask = JParserBuildTasks.hostBuildProjectTask(":jParser:runtime:builder", "runtime_helper", "jni")
val libAJniBuildTask = JParserBuildTasks.hostBuildProjectTask(":examples:SharedLib:libA:builder", "LibA", "jni")
val libBJniBuildTask = JParserBuildTasks.hostBuildProjectTask(":examples:SharedLib:libB:builder", "LibB", "jni")

dependencies {
    implementation(project(":examples:SharedLib:app:core"))

    implementation(variantOf(libs.gdxPlatform) { classifier("natives-desktop") })
    implementation(libs.gdxBackendLwjgl3)

    implementation(project(":examples:SharedLib:libA:desktop:LibA-desktop-jni"))
    implementation(project(":examples:SharedLib:libB:desktop:LibB-desktop-jni"))

    implementation(project(":jParser:runtime:desktop:runtime-desktop-jni"))

    testImplementation(libs.junit)
}

tasks.test {
    useJUnit()
    systemProperty("java.awt.headless", "true")
    dependsOn(
        runtimeJniBuildTask,
        libAJniBuildTask,
        libBJniBuildTask,
        ":examples:SharedLib:libA:desktop:LibA-desktop-jni:assemble",
        ":examples:SharedLib:libB:desktop:LibB-desktop-jni:assemble"
    )
    testLogging {
        showStandardStreams = true
        events("passed", "skipped", "failed")
    }
    if(isMacOs) {
        jvmArgs("-XstartOnFirstThread")
    }
}

tasks.named("test") {
    outputs.upToDateWhen { false }
}

tasks.register<JavaExec>("SharedLib_run_app_desktop_jni") {
    group = "example-desktop"
    description = "Run desktop app with JNI bridge"
    dependsOn(
        runtimeJniBuildTask,
        libAJniBuildTask,
        libBJniBuildTask
    )
    mainClass.set("com.github.xpenatan.jParser.example.app.Main")
    classpath = sourceSets["main"].runtimeClasspath
    if(isMacOs) {
        jvmArgs("-XstartOnFirstThread")
    }
}
