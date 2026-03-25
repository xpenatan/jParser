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
    compileOnly(project(":examples:TestLib:lib:lib-core"))

    implementation("com.badlogicgames.gdx:gdx:${LibExt.gdxVersion}")
}