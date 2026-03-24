plugins {
    id("java")
}

java {
    sourceCompatibility = JavaVersion.toVersion(LibExt.java11Target)
    targetCompatibility = JavaVersion.toVersion(LibExt.java11Target)
}

val mainClassName = "BuildLibB"

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
        implementation(project(":jParser:jParser-ffm"))
    }

    implementation(project(":idl-helper:idl-helper-core"))
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

// FFM tasks — generate FFM Java code and/or compile native libs with FFMGlue

tasks.register<JavaExec>("LibB_build_project_ffm") {
    group = "lib"
    description = "Generate FFM Java code only (no native compilation)"
    mainClass.set(mainClassName)
    args = mutableListOf("ffm")
    classpath = sourceSets["main"].runtimeClasspath
}

tasks.register<JavaExec>("LibB_build_project_ffm_windows64") {
    group = "lib"
    description = "Generate FFM Java code and compile for Windows with FFMGlue"
    mainClass.set(mainClassName)
    args = mutableListOf("ffm", "ffm_windows64")
    classpath = sourceSets["main"].runtimeClasspath
}

tasks.register<JavaExec>("LibB_build_project_ffm_linux64") {
    group = "lib"
    description = "Generate FFM Java code and compile for Linux with FFMGlue"
    mainClass.set(mainClassName)
    args = mutableListOf("ffm", "ffm_linux64")
    classpath = sourceSets["main"].runtimeClasspath
}

tasks.register<JavaExec>("LibB_build_project_ffm_mac64") {
    group = "lib"
    description = "Generate FFM Java code and compile for Mac with FFMGlue"
    mainClass.set(mainClassName)
    args = mutableListOf("ffm", "ffm_mac64")
    classpath = sourceSets["main"].runtimeClasspath
}

tasks.register<JavaExec>("LibB_build_project_ffm_macArm") {
    group = "lib"
    description = "Generate FFM Java code and compile for Mac ARM with FFMGlue"
    mainClass.set(mainClassName)
    args = mutableListOf("ffm", "ffm_macArm")
    classpath = sourceSets["main"].runtimeClasspath
}
