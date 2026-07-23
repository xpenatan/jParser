import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.nativeplatform.platform.internal.DefaultNativePlatform

plugins {
    id("java")
}

sourceSets["test"].java.srcDir(rootProject.file("examples/SharedLib/app/core/src/test/java"))

java {
    sourceCompatibility = JavaVersion.toVersion(libs.versions.javaFfm.get())
    targetCompatibility = JavaVersion.toVersion(libs.versions.javaFfm.get())
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(libs.versions.javaFfm.get()))
    }
}

val isMacOs = DefaultNativePlatform.getCurrentOperatingSystem().isMacOsX
val runtimeFfmBuildTask = JParserBuildTasks.hostBuildProjectTask(":jParser:runtime:builder", "runtime_helper", "ffm")
val libAFfmBuildTask = JParserBuildTasks.hostBuildProjectTask(":examples:SharedLib:libA:builder", "LibA", "ffm")
val libBFfmBuildTask = JParserBuildTasks.hostBuildProjectTask(":examples:SharedLib:libB:builder", "LibB", "ffm")

dependencies {
    implementation(project(":examples:SharedLib:app:core"))

    implementation(variantOf(libs.gdxPlatform) { classifier("natives-desktop") })
    implementation(libs.gdxBackendLwjgl3)

    implementation(project(":examples:SharedLib:libA:desktop:LibA-desktop-ffm"))
    implementation(project(":examples:SharedLib:libB:desktop:LibB-desktop-ffm"))

    implementation(project(":jParser:runtime:desktop:runtime-desktop-ffm"))

    testImplementation(libs.junit)
}

tasks.test {
    useJUnit()
    systemProperty("java.awt.headless", "true")
    dependsOn(
        runtimeFfmBuildTask,
        libAFfmBuildTask,
        libBFfmBuildTask,
        ":examples:SharedLib:libA:desktop:LibA-desktop-ffm:assemble",
        ":examples:SharedLib:libB:desktop:LibB-desktop-ffm:assemble"
    )
    javaLauncher.set(javaToolchains.launcherFor {
        languageVersion.set(JavaLanguageVersion.of(libs.versions.javaFfm.get()))
    })
    jvmArgs("--enable-native-access=ALL-UNNAMED")
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

tasks.register<JavaExec>("SharedLib_run_app_desktop_ffm") {
    group = "example-desktop"
    description = "Run desktop app with FFM bridge"
    dependsOn(
        runtimeFfmBuildTask,
        libAFfmBuildTask,
        libBFfmBuildTask
    )
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
