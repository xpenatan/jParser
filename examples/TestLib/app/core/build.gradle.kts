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

    // Tests in core (DesktopHeadlessTest) are compiled here so app modules can
    // reuse the test class without duplicating sources. Add test dependencies
    // required for compiling the headless integration test.
    testImplementation("junit:junit:${LibExt.jUnitVersion}")
    testImplementation(project(":examples:TestLib:lib:lib-base"))
    testImplementation(project(":examples:TestLib:lib:lib-core"))
    testCompileOnly(project(":idl-helper:idl-helper-core"))
}