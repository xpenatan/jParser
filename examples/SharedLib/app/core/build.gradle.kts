plugins {
    id("java")
}

java {
    sourceCompatibility = JavaVersion.toVersion(LibExt.javaMainTarget)
    targetCompatibility = JavaVersion.toVersion(LibExt.javaMainTarget)
}

dependencies {
    // compileOnly: app/core compiles against core's API, but does NOT
    // propagate it transitively. Each platform module (desktop-jni, desktop-ffm,
    // android, teavm) provides the actual native bridge implementation.
    compileOnly(project(":examples:SharedLib:libA:core"))
    compileOnly(project(":examples:SharedLib:libB:core"))

    implementation("com.badlogicgames.gdx:gdx:${LibExt.gdxVersion}")

    testImplementation(project(":examples:SharedLib:libA:shared:jni"))
    testImplementation(project(":examples:SharedLib:libB:shared:jni"))
    testImplementation(project(":jParser:runtime:runtime-jvm:jni"))
    testImplementation("junit:junit:${LibExt.jUnitVersion}")
}

tasks.named<Test>("test") {
    testLogging.showStandardStreams = true
}
