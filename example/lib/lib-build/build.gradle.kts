plugins {
    id("java")
}

java {
    sourceCompatibility = JavaVersion.toVersion(LibExt.java8Target)
    targetCompatibility = JavaVersion.toVersion(LibExt.java8Target)
}

val mainClassName = "BuildLib"

dependencies {
    implementation(project(":example:lib:lib-base"))

    if(LibExt.exampleUseRepoLibs) {
        implementation("com.github.xpenatan.jParser:core:${LibExt.libVersion}")
        implementation("com.github.xpenatan.jParser:idl:${LibExt.libVersion}")
        implementation("com.github.xpenatan.jParser:teavm:${LibExt.libVersion}")
        implementation("com.github.xpenatan.jParser:cpp:${LibExt.libVersion}")
        implementation("com.github.xpenatan.jParser:builder:${LibExt.libVersion}")
        implementation("com.github.xpenatan.jParser:builder-tool:${LibExt.libVersion}")
    }
    else {
        implementation(project(":jParser:jParser-core"))
        implementation(project(":jParser:jParser-idl"))
        implementation(project(":jParser:jParser-teavm"))
        implementation(project(":jParser:jParser-cpp"))
        implementation(project(":jParser:jParser-build"))
        implementation(project(":jParser:jParser-build-tool"))
    }
}

tasks.register<JavaExec>("build_project") {
    group = "lib"
    description = "Generate native project"
    mainClass.set(mainClassName)
    args = mutableListOf()
    classpath = sourceSets["main"].runtimeClasspath
}

tasks.register<JavaExec>("build_project_all") {
    group = "lib"
    description = "Generate native project"
    mainClass.set(mainClassName)
    args = mutableListOf("teavm", "windows64", "linux64", "mac64", "macArm", "android", "ios")
    classpath = sourceSets["main"].runtimeClasspath
}

tasks.register<JavaExec>("build_project_teavm") {
    group = "lib"
    description = "Generate native project"
    mainClass.set(mainClassName)
    args = mutableListOf("teavm")
    classpath = sourceSets["main"].runtimeClasspath
}

tasks.register<JavaExec>("build_project_windows64") {
    group = "lib"
    description = "Generate native project"
    mainClass.set(mainClassName)
    args = mutableListOf("windows64")
    classpath = sourceSets["main"].runtimeClasspath
}

tasks.register<JavaExec>("build_project_linux64") {
    group = "lib"
    description = "Generate native project"
    mainClass.set(mainClassName)
    args = mutableListOf("linux64")
    classpath = sourceSets["main"].runtimeClasspath
}

tasks.register<JavaExec>("build_project_mac64") {
    group = "lib"
    description = "Generate native project"
    mainClass.set(mainClassName)
    args = mutableListOf("mac64")
    classpath = sourceSets["main"].runtimeClasspath
}

tasks.register<JavaExec>("build_project_macArm") {
    group = "lib"
    description = "Generate native project"
    mainClass.set(mainClassName)
    args = mutableListOf("macArm")
    classpath = sourceSets["main"].runtimeClasspath
}

tasks.register<JavaExec>("build_project_android") {
    group = "lib"
    description = "Generate native project"
    mainClass.set(mainClassName)
    args = mutableListOf("android")
    classpath = sourceSets["main"].runtimeClasspath
}

tasks.register<JavaExec>("build_project_ios") {
    group = "lib"
    description = "Generate native project"
    mainClass.set(mainClassName)
    args = mutableListOf("ios")
    classpath = sourceSets["main"].runtimeClasspath
}