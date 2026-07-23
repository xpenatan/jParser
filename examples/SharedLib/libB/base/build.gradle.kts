plugins {
    id("java")
}

java {
    sourceCompatibility = JavaVersion.toVersion(libs.versions.javaMain.get())
    targetCompatibility = JavaVersion.toVersion(libs.versions.javaMain.get())
}

dependencies {
    implementation(project(":jParser:loader:loader-core"))
    implementation(project(":jParser:api:api-core"))
}