import org.gradle.nativeplatform.platform.internal.DefaultNativePlatform

dependencies {
    implementation(project(":example:app:core"))
    implementation(project(":example:lib:lib-desktop"))

    implementation("com.badlogicgames.gdx:gdx-platform:${LibExt.gdxVersion}:natives-desktop")
    implementation("com.badlogicgames.gdx:gdx-backend-lwjgl3:${LibExt.gdxVersion}")
}

val mainClassName = "com.github.xpenatan.jparser.example.app.Main"

tasks.register<JavaExec>("run-app-desktop") {
    group = "example-desktop"
    description = "Run desktop app"
    mainClass.set(mainClassName)
    classpath = sourceSets["main"].runtimeClasspath

    if(DefaultNativePlatform.getCurrentOperatingSystem().isMacOsX) {
        jvmArgs("-XstartOnFirstThread")
    }
}