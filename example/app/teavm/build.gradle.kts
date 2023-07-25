plugins {
    id("org.gretty") version("3.1.0")
}
//
gretty {
    contextPath = '/'
    extraResourceBase("build/dist/webapp")
}

dependencies {
    implementation(project(":example:app:core"))
    implementation(project(":example:lib:teavm"))

    implementation("com.badlogicgames.gdx:gdx:${LibExt.gdxVersion}")
    implementation("com.github.xpenatan.gdx-teavm:backend-teavm:1.0.0-SNAPSHOT")
}

val mainClassName = "com.github.xpenatan.jparser.example.app.Build"

tasks.register<JavaExec>("build-app") {
    group = "example-teavm"
    description = "Build teavm app"
    mainClass.set(mainClassName)
    classpath = sourceSets["main"].runtimeClasspath
}

val tasksOrder = tasks.register<GradleBuild>("tasksOrder") {
    tasks = listOf(
        ":example:app:teavm:build-app",
        ":example:app:teavm:jettyRun"
    )
}

tasks.register("run-app") {
    group = "example-teavm"
    description = "Run teavm app"
    val list = listOf("build-app", "jettyRun")
    dependsOn(list)
    mustRunAfter(list)
}