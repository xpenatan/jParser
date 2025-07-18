import org.gradle.nativeplatform.platform.internal.DefaultNativePlatform

dependencies {
    implementation(project(":example:app:core"))
    implementation(project(":example:lib:lib-core"))
    implementation(project(":example:lib:lib-desktop"))

    implementation("com.badlogicgames.gdx:gdx-platform:${LibExt.gdxVersion}:natives-desktop")
    implementation("com.badlogicgames.gdx:gdx-backend-lwjgl3:${LibExt.gdxVersion}")
}

tasks.register<JavaExec>("run-app-desktop") {
    group = "example-desktop"
    description = "Run desktop app"
    mainClass.set("com.github.xpenatan.jparser.example.app.Main")
    classpath = sourceSets["main"].runtimeClasspath

    if(DefaultNativePlatform.getCurrentOperatingSystem().isMacOsX) {
        jvmArgs("-XstartOnFirstThread")
    }
}

tasks.register<JavaExec>("run-benchmark-desktop") {
    group = "example-desktop"
    description = "Run desktop app"
    mainClass.set("com.github.xpenatan.jparser.example.app.BenchmarkMain")
    classpath = sourceSets["main"].runtimeClasspath

    if(DefaultNativePlatform.getCurrentOperatingSystem().isMacOsX) {
        jvmArgs("-XstartOnFirstThread")
    }
}