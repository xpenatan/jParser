plugins {
    id("java")
}

val mainClassName = "BuildLib"

dependencies {
    implementation(project(":example:lib-test:lib-base"))

    if(LibExt.exampleUseRepoLibs) {
        implementation("com.github.xpenatan.jParser:core:${LibExt.libVersion}")
        implementation("com.github.xpenatan.jParser:idl:${LibExt.libVersion}")
        implementation("com.github.xpenatan.jParser:teavm:${LibExt.libVersion}")
        implementation("com.github.xpenatan.jParser:cpp:${LibExt.libVersion}")
        implementation("com.github.xpenatan.jParser:builder:${LibExt.libVersion}")
        implementation("com.github.xpenatan.jParser:builder-tool:${LibExt.libVersion}")
    }
    else {
        implementation(project(":jParser:core"))
        implementation(project(":jParser:idl"))
        implementation(project(":jParser:teavm"))
        implementation(project(":jParser:cpp"))
        implementation(project(":jParser:builder"))
        implementation(project(":jParser:builder-tool"))
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