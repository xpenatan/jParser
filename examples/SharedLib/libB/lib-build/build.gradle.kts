plugins {
    id("java")
}

java {
    sourceCompatibility = JavaVersion.toVersion(LibExt.java11Target)
    targetCompatibility = JavaVersion.toVersion(LibExt.java11Target)
}

val mainClassName = "BuildLib"

dependencies {
    implementation(project(":examples:SharedLib:libB:lib-base"))
    implementation(project(":examples:SharedLib:libA:lib-core"))


    if(LibExt.exampleUseRepoLibs) {
        implementation("com.github.xpenatan.jParser:jParser-core:-SNAPSHOT")
        implementation("com.github.xpenatan.jParser:jParser-idl:-SNAPSHOT")
        implementation("com.github.xpenatan.jParser:jParser-teavm:-SNAPSHOT")
        implementation("com.github.xpenatan.jParser:jParser-cpp:-SNAPSHOT")
        implementation("com.github.xpenatan.jParser:jParser-build:-SNAPSHOT")
        implementation("com.github.xpenatan.jParser:jParser-build-tool:-SNAPSHOT")
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

tasks.register<JavaExec>("LibB_build_project") {
    group = "lib"
    description = "Generate native project"
    mainClass.set(mainClassName)
    args = mutableListOf()
    classpath = sourceSets["main"].runtimeClasspath
}

tasks.register<JavaExec>("LibB_build_project_all") {
    group = "lib"
    description = "Generate native project"
    mainClass.set(mainClassName)
    args = mutableListOf("teavm", "windows64", "linux64", "mac64", "macArm", "android", "ios")
    classpath = sourceSets["main"].runtimeClasspath
}

tasks.register<JavaExec>("LibB_build_project_teavm") {
    group = "lib"
    description = "Generate native project"
    mainClass.set(mainClassName)
    args = mutableListOf("teavm")
    classpath = sourceSets["main"].runtimeClasspath
}

tasks.register<JavaExec>("LibB_build_project_windows64") {
    group = "lib"
    description = "Generate native project"
    mainClass.set(mainClassName)
    args = mutableListOf("windows64")
    classpath = sourceSets["main"].runtimeClasspath
}

tasks.register<JavaExec>("LibB_build_project_linux64") {
    group = "lib"
    description = "Generate native project"
    mainClass.set(mainClassName)
    args = mutableListOf("linux64")
    classpath = sourceSets["main"].runtimeClasspath
}

tasks.register<JavaExec>("LibB_build_project_mac64") {
    group = "lib"
    description = "Generate native project"
    mainClass.set(mainClassName)
    args = mutableListOf("mac64")
    classpath = sourceSets["main"].runtimeClasspath
}

tasks.register<JavaExec>("LibB_build_project_macArm") {
    group = "lib"
    description = "Generate native project"
    mainClass.set(mainClassName)
    args = mutableListOf("macArm")
    classpath = sourceSets["main"].runtimeClasspath
}

tasks.register<JavaExec>("LibB_build_project_android") {
    group = "lib"
    description = "Generate native project"
    mainClass.set(mainClassName)
    args = mutableListOf("android")
    classpath = sourceSets["main"].runtimeClasspath
}

tasks.register<JavaExec>("LibB_build_project_ios") {
    group = "lib"
    description = "Generate native project"
    mainClass.set(mainClassName)
    args = mutableListOf("ios")
    classpath = sourceSets["main"].runtimeClasspath
}