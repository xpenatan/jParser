plugins {
    id("java")
}

java {
    sourceCompatibility = JavaVersion.toVersion(LibExt.javaMainTarget)
    targetCompatibility = JavaVersion.toVersion(LibExt.javaMainTarget)
}

dependencies {
    implementation(project(":jParser:loader:loader-core"))
    implementation(project(":jParser:api:api-core"))
    implementation(project(":jParser:runtime:runtime-core"))
}