plugins {
    id("java")
}

java {
    sourceCompatibility = JavaVersion.toVersion(LibExt.java8Target)
    targetCompatibility = JavaVersion.toVersion(LibExt.java8Target)
}

dependencies {
    implementation(project(":loader:loader-core"))
    implementation(project(":idl:idl-core"))
    implementation(project(":idl-helper:idl-helper-core"))
}