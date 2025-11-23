plugins {
    id("java")
}

java {
    sourceCompatibility = JavaVersion.toVersion(LibExt.java8Target)
    targetCompatibility = JavaVersion.toVersion(LibExt.java8Target)
}

dependencies {
    if(LibExt.exampleUseRepoLibs) {
        implementation("com.github.xpenatan.jParser:jParser-base:-SNAPSHOT")
        implementation("com.github.xpenatan.jParser:loader-core:-SNAPSHOT")
    }
    else {
        implementation(project(":jParser:jParser-base"))
        implementation(project(":loader:loader-core"))
        implementation(project(":idl:idl-core"))
    }
}