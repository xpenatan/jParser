plugins {
    id("java")
}

val mainClassName = "BuildLib"

dependencies {
    implementation(project(":example:lib:lib-base"))
    implementation(project(":example:lib-ext:ext-build"))

    if(LibExt.exampleUseRepoLibs) {
        implementation("com.github.xpenatan.jParser:core:${LibExt.libVersion}")
        implementation("com.github.xpenatan.jParser:idl:${LibExt.libVersion}")
        implementation("com.github.xpenatan.jParser:teavm:${LibExt.libVersion}")
        implementation("com.github.xpenatan.jParser:cpp:${LibExt.libVersion}")
    }
    else {
        implementation(project(":jParser:core"))
        implementation(project(":jParser:idl"))
        implementation(project(":jParser:teavm"))
        implementation(project(":jParser:cpp"))
        implementation(project(":jParser:builder"))
    }
}

tasks.register<JavaExec>("build_project") {
    group = "lib"
    description = "Generate native project"
    mainClass.set(mainClassName)
    args = mutableListOf("teavm", "windows", "linux", "mac", "android")
    classpath = sourceSets["main"].runtimeClasspath
}

tasks.register<JavaExec>("build_project_windows") {
    group = "lib"
    description = "Generate native project"
    mainClass.set(mainClassName)
    args = mutableListOf("windows")
    classpath = sourceSets["main"].runtimeClasspath
}

tasks.register<JavaExec>("build_project_linux") {
    group = "lib"
    description = "Generate native project"
    mainClass.set(mainClassName)
    args = mutableListOf("linux")
    classpath = sourceSets["main"].runtimeClasspath
}

tasks.register<JavaExec>("build_project_mac") {
    group = "lib"
    description = "Generate native project"
    mainClass.set(mainClassName)
    args = mutableListOf("mac")
    classpath = sourceSets["main"].runtimeClasspath
}

tasks.register<JavaExec>("build_project_android") {
    group = "lib"
    description = "Generate native project"
    mainClass.set(mainClassName)
    args = mutableListOf("android")
    classpath = sourceSets["main"].runtimeClasspath
}

tasks.register<JavaExec>("build_project_teavm") {
    group = "lib"
    description = "Generate native project"
    mainClass.set(mainClassName)
    args = mutableListOf("teavm")
    classpath = sourceSets["main"].runtimeClasspath
}