plugins {
    id("java")
}

java {
    sourceCompatibility = JavaVersion.toVersion(LibExt.java8Target)
    targetCompatibility = JavaVersion.toVersion(LibExt.java8Target)
}

dependencies {
    if(LibExt.exampleUseRepoLibs) {
        implementation("com.github.xpenatan.jParser:base:${LibExt.libVersion}")
        implementation("com.github.xpenatan.jParser:loader-core:${LibExt.libVersion}")
    }
    else {
        implementation(project(":jParser:base"))
        implementation(project(":jParser:loader:loader-core"))
    }
}