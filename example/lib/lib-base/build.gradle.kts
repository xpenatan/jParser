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
        implementation(project(":jParser:jParser-base"))
        implementation(project(":loader:loader-core"))
    }
}