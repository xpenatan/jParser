plugins {
    id("org.gretty") version("3.1.0")
}

gretty {
    contextPath = "/"
    extraResourceBase("build/dist/webapp")
}

dependencies {
    implementation(project(":example:app-test:core"))
    implementation(project(":example:lib-test:lib-teavm"))

    implementation("com.badlogicgames.gdx:gdx:${LibExt.gdxVersion}")
    implementation("com.github.xpenatan.gdx-teavm:backend-teavm:${LibExt.gdxTeaVMVersion}")
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
    val list = listOf("build-app-teavm", "jettyRun")
    dependsOn(list)

    tasks.findByName("jettyRun")?.mustRunAfter("build-app-teavm")
}