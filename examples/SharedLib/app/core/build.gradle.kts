plugins {
    id("java")
}

java {
    sourceCompatibility = JavaVersion.toVersion(libs.versions.javaMain.get())
    targetCompatibility = JavaVersion.toVersion(libs.versions.javaMain.get())
}

dependencies {
    // compileOnly: app/core compiles against core's API, but does NOT
    // propagate it transitively. Each platform module (desktop-jni, desktop-ffm,
    // android, teavm) provides the actual native bridge implementation.
    compileOnly(project(":examples:SharedLib:libA:core"))
    compileOnly(project(":examples:SharedLib:libB:core"))

    implementation(libs.gdxCore)

    testImplementation(project(":examples:SharedLib:libA:shared:LibA-jni"))
    testImplementation(project(":examples:SharedLib:libB:shared:LibB-jni"))
    testImplementation(project(":jParser:runtime:shared:runtime-jni"))
    testImplementation(libs.junit)
}

tasks.named<Test>("test") {
    testLogging.showStandardStreams = true
}
