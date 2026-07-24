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
val hostOs = System.getProperty("os.name")
val hostArch = System.getProperty("os.arch")
val hostTarget = when {
    hostOs.startsWith("Windows") -> "windows64"
    hostOs == "Linux" && (hostArch == "x86_64" || hostArch == "amd64") -> "linux64"
    hostOs.startsWith("Mac") && (hostArch == "aarch64" || hostArch == "arm64") -> "macArm"
    hostOs.startsWith("Mac") && (hostArch == "x86_64" || hostArch == "amd64") -> "mac64"
    else -> error("Unsupported desktop host: os=$hostOs arch=$hostArch")
}

val runtimeFfmBuildTask = ":jParser:runtime:builder:runtime_helper_build_project_${hostTarget}_ffm"
val libAFfmBuildTask = ":examples:SharedLib:libA:builder:LibA_build_project_${hostTarget}_ffm"
val libBFfmBuildTask = ":examples:SharedLib:libB:builder:LibB_build_project_${hostTarget}_ffm"

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
