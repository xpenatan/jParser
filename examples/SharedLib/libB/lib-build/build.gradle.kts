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

    implementation(project(":jParser:jParser-core"))
    implementation(project(":jParser:jParser-idl"))
    implementation(project(":jParser:jParser-teavm"))
    implementation(project(":jParser:jParser-jni"))
    implementation(project(":jParser:jParser-build"))
    implementation(project(":jParser:jParser-build-tool"))
    implementation(project(":jParser:jParser-ffm"))

    implementation(project(":idl-helper:idl-helper-core"))
}

tasks.register<JavaExec>("LibB_build_project") {
    group = "lib"
    description = "Generate native project"
    mainClass.set(mainClassName)
    args = mutableListOf()
    classpath = sourceSets["main"].runtimeClasspath
}

tasks.register<JavaExec>("LibB_build_project_teavm") {
    group = "lib"
    description = "Generate native project"
    mainClass.set(mainClassName)
    args = mutableListOf("teavm")
    classpath = sourceSets["main"].runtimeClasspath
}

tasks.register<JavaExec>("LibB_build_project_windows64_jni") {
    group = "lib"
    description = "Generate native project"
    mainClass.set(mainClassName)
    args = mutableListOf("windows64_jni")
    classpath = sourceSets["main"].runtimeClasspath
}

tasks.register<JavaExec>("LibB_build_project_linux64_jni") {
    group = "lib"
    description = "Generate native project"
    mainClass.set(mainClassName)
    args = mutableListOf("linux64_jni")
    classpath = sourceSets["main"].runtimeClasspath
}

tasks.register<JavaExec>("LibB_build_project_mac64_jni") {
    group = "lib"
    description = "Generate native project"
    mainClass.set(mainClassName)
    args = mutableListOf("mac64_jni")
    classpath = sourceSets["main"].runtimeClasspath
}

tasks.register<JavaExec>("LibB_build_project_macArm_jni") {
    group = "lib"
    description = "Generate native project"
    mainClass.set(mainClassName)
    args = mutableListOf("macArm_jni")
    classpath = sourceSets["main"].runtimeClasspath
}

tasks.register<JavaExec>("LibB_build_project_android_jni") {
    group = "lib"
    description = "Generate native project"
    mainClass.set(mainClassName)
    args = mutableListOf("android_jni")
    classpath = sourceSets["main"].runtimeClasspath
}

tasks.register<JavaExec>("LibB_build_project_ios_jni") {
    group = "lib"
    description = "Generate native project"
    mainClass.set(mainClassName)
    args = mutableListOf("ios_jni")
    classpath = sourceSets["main"].runtimeClasspath
}

// FFM tasks — generate FFM Java code and/or compile native libs with FFMGlue

tasks.register<JavaExec>("LibB_build_project_windows64_ffm") {
    group = "lib"
    description = "Generate FFM Java code and compile for Windows with FFMGlue"
    mainClass.set(mainClassName)
    args = mutableListOf("windows64_ffm")
    classpath = sourceSets["main"].runtimeClasspath
}

tasks.register<JavaExec>("LibB_build_project_linux64_ffm") {
    group = "lib"
    description = "Generate FFM Java code and compile for Linux with FFMGlue"
    mainClass.set(mainClassName)
    args = mutableListOf("linux64_ffm")
    classpath = sourceSets["main"].runtimeClasspath
}

tasks.register<JavaExec>("LibB_build_project_mac64_ffm") {
    group = "lib"
    description = "Generate FFM Java code and compile for Mac with FFMGlue"
    mainClass.set(mainClassName)
    args = mutableListOf("mac64_ffm")
    classpath = sourceSets["main"].runtimeClasspath
}

tasks.register<JavaExec>("LibB_build_project_macArm_ffm") {
    group = "lib"
    description = "Generate FFM Java code and compile for Mac ARM with FFMGlue"
    mainClass.set(mainClassName)
    args = mutableListOf("macArm_ffm")
    classpath = sourceSets["main"].runtimeClasspath
}
