plugins {
    id("org.gretty") version("3.1.0")
}

gretty {
    contextPath = "/"
    extraResourceBase("build/dist/webapp")
}

dependencies {
    implementation(project(":example:app:core"))
    implementation(project(":example:lib:teavm"))

    implementation("com.badlogicgames.gdx:gdx:${LibExt.gdxVersion}")
    implementation("com.github.xpenatan.gdx-teavm:backend-teavm:1.0.0-SNAPSHOT")
}

val mainClassName = "Build"

tasks.register<JavaExec>("build-app-teavm") {
    group = "example-teavm"
    description = "Build teavm app"
    mainClass.set(mainClassName)
    classpath = sourceSets["main"].runtimeClasspath
}

tasks.register("run-app-teavm") {
    group = "example-teavm"
    description = "Run teavm app"
    val list = listOf(":buildAll", "build-app-teavm", "jettyRun")
    dependsOn(list)

    tasks.findByName("build-app-teavm")?.mustRunAfter(":buildAll")
    tasks.findByName("jettyRun")?.mustRunAfter("build-app-teavm")
}