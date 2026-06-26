import org.gradle.nativeplatform.platform.internal.DefaultNativePlatform

plugins {
    id("java")
}

sourceSets["test"].java.srcDir(rootProject.file("examples/SharedLib/app/core/src/test/java"))

java {
    sourceCompatibility = JavaVersion.toVersion(LibExt.javaMainTarget)
    targetCompatibility = JavaVersion.toVersion(LibExt.javaMainTarget)
}

val isMacOs = DefaultNativePlatform.getCurrentOperatingSystem().isMacOsX

dependencies {
    implementation(project(":examples:SharedLib:app:core"))

    implementation("com.badlogicgames.gdx:gdx-platform:${LibExt.gdxVersion}:natives-desktop")
    implementation("com.badlogicgames.gdx:gdx-backend-lwjgl3:${LibExt.gdxVersion}")

    implementation(project(":examples:SharedLib:libA:lib-jni"))
    implementation(project(":examples:SharedLib:libB:lib-jni"))

    implementation(project(":jParser:runtime:runtime-jvm:jni"))

    testImplementation("junit:junit:${LibExt.jUnitVersion}")
}

tasks.test {
    useJUnit()
    systemProperty("java.awt.headless", "true")
    dependsOn(
        ":jParser:runtime:plugin:jParser_build_windows64_jni",
        ":examples:SharedLib:libA:plugin:jParser_build_windows64_jni",
        ":examples:SharedLib:libB:plugin:jParser_build_windows64_jni",
        ":examples:SharedLib:libA:lib-jni:assemble",
        ":examples:SharedLib:libB:lib-jni:assemble"
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
        ":jParser:runtime:plugin:jParser_build_windows64_jni",
        ":examples:SharedLib:libA:plugin:jParser_build_windows64_jni",
        ":examples:SharedLib:libB:plugin:jParser_build_windows64_jni"
    )
    mainClass.set("com.github.xpenatan.jParser.example.app.Main")
    classpath = sourceSets["main"].runtimeClasspath
    if(isMacOs) {
        jvmArgs("-XstartOnFirstThread")
    }
}
