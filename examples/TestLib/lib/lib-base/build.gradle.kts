plugins {
    id("java")
}

java {
    sourceCompatibility = JavaVersion.toVersion(LibExt.java8Target)
    targetCompatibility = JavaVersion.toVersion(LibExt.java8Target)
}

dependencies {
    implementation(project(":loader:loader-core"))
    implementation(project(":idl:api:api-core"))
    implementation(project(":idl:runtime:runtime-core"))
}