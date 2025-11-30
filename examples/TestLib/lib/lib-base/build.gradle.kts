plugins {
    id("java")
}

java {
    sourceCompatibility = JavaVersion.toVersion(LibExt.java8Target)
    targetCompatibility = JavaVersion.toVersion(LibExt.java8Target)
}

dependencies {
    if(LibExt.exampleUseRepoLibs) {
        implementation("com.github.xpenatan.jParser:loader-core:-SNAPSHOT")
        implementation("com.github.xpenatan.jParser:idl-core:-SNAPSHOT")
        implementation("com.github.xpenatan.jParser:idl-helper-core:-SNAPSHOT")
    }
    else {
        implementation(project(":loader:loader-core"))
        implementation(project(":idl:idl-core"))
        implementation(project(":idl-helper:idl-helper-core"))
    }
}