import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.nativeplatform.platform.internal.DefaultNativePlatform

plugins {
    id("java")
}

sourceSets["test"].java.srcDir(rootProject.file("examples/SharedLib/app/core/src/test/java"))

java {
    sourceCompatibility = JavaVersion.toVersion(LibExt.javaFFMTarget)
    targetCompatibility = JavaVersion.toVersion(LibExt.javaFFMTarget)
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(LibExt.javaFFMTarget))
    }
}

val isMacOs = DefaultNativePlatform.getCurrentOperatingSystem().isMacOsX
val runtimeFfmBuildTask = LibExt.hostBuildProjectTask(":jParser:runtime:runtime-build", "runtime_helper", "ffm")
val libAFfmBuildTask = LibExt.hostBuildProjectTask(":examples:SharedLib:libA:lib-build", "LibA", "ffm")
val libBFfmBuildTask = LibExt.hostBuildProjectTask(":examples:SharedLib:libB:lib-build", "LibB", "ffm")

dependencies {
    implementation(project(":examples:SharedLib:app:core"))

    implementation("com.badlogicgames.gdx:gdx-platform:${LibExt.gdxVersion}:natives-desktop")
    implementation("com.badlogicgames.gdx:gdx-backend-lwjgl3:${LibExt.gdxVersion}")

    implementation(project(":examples:SharedLib:libA:lib-ffm"))
    implementation(project(":examples:SharedLib:libB:lib-ffm"))

    implementation(project(":jParser:runtime:runtime-jvm:ffm"))

    testImplementation("junit:junit:${LibExt.jUnitVersion}")
}

tasks.test {
    useJUnit()
    systemProperty("java.awt.headless", "true")
    dependsOn(
        runtimeFfmBuildTask,
        libAFfmBuildTask,
        libBFfmBuildTask,
        ":examples:SharedLib:libA:lib-ffm:assemble",
        ":examples:SharedLib:libB:lib-ffm:assemble"
    )
    javaLauncher.set(javaToolchains.launcherFor {
        languageVersion.set(JavaLanguageVersion.of(LibExt.javaFFMTarget))
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
        languageVersion.set(JavaLanguageVersion.of(LibExt.javaFFMTarget))
    })
    jvmArgs("--enable-native-access=ALL-UNNAMED")
    if(isMacOs) {
        jvmArgs("-XstartOnFirstThread")
    }
}
