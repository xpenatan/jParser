plugins {
    id("java")
}

java {
    sourceCompatibility = JavaVersion.toVersion(libs.versions.javaMain.get())
    targetCompatibility = JavaVersion.toVersion(libs.versions.javaMain.get())
}

dependencies {
    compileOnly(project(":examples:TestLib:lib:core"))

    implementation(libs.gdxCore)

    testImplementation(libs.junit)
    testImplementation(project(":examples:TestLib:lib:base"))
    testImplementation(project(":examples:TestLib:lib:core"))
    testCompileOnly(project(":jParser:runtime:core"))
}