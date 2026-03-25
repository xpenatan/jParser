plugins {
    id("java")
}

java {
    sourceCompatibility = JavaVersion.toVersion(LibExt.java8Target)
    targetCompatibility = JavaVersion.toVersion(LibExt.java8Target)
}

dependencies {
    // compileOnly: app/core compiles against lib-core's API, but does NOT
    // propagate it transitively. Each platform module (desktop-jni, desktop-ffm,
    // android, teavm) provides the actual native bridge implementation.
    compileOnly(project(":examples:SharedLib:libA:lib-core"))
    compileOnly(project(":examples:SharedLib:libB:lib-core"))

    implementation("com.badlogicgames.gdx:gdx:${LibExt.gdxVersion}")

    testImplementation(project(":examples:SharedLib:libA:lib-desktop-jni"))
    testImplementation(project(":examples:SharedLib:libB:lib-desktop-jni"))
    testImplementation(project(":idl-helper:idl-helper-desktop-jni"))
    testImplementation("junit:junit:${LibExt.jUnitVersion}")
}

tasks.named<Test>("test") {
    testLogging.showStandardStreams = true
}