dependencies {
    implementation(project(":example:app:core"))
    implementation(project(":example:lib:lib-example-desktop"))

    implementation("com.badlogicgames.gdx:gdx-platform:${LibExt.gdxVersion}:natives-desktop")
    implementation("com.badlogicgames.gdx:gdx-backend-lwjgl:${LibExt.gdxVersion}")
}

val mainClassName = "com.github.xpenatan.jparser.example.app.Main"

tasks.register<JavaExec>("run-app") {
    group = "example-desktop"
    description = "Run desktop app"
    mainClass.set(mainClassName)
    classpath = sourceSets["main"].runtimeClasspath
}