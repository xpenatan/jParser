plugins {
    id("java")
}

java {
    sourceCompatibility = JavaVersion.toVersion(LibExt.java11Target)
    targetCompatibility = JavaVersion.toVersion(LibExt.java11Target)
}

val mainClassName = "BuildIDLHelper"

dependencies {
    implementation(project(":idl-helper:idl-helper-base"))
    implementation(project(":jParser:jParser-core"))
    implementation(project(":jParser:jParser-idl"))
    implementation(project(":jParser:jParser-teavm"))
    implementation(project(":jParser:jParser-jni"))
    implementation(project(":jParser:jParser-ffm"))
    implementation(project(":jParser:jParser-build"))
    implementation(project(":jParser:jParser-build-tool"))
}

tasks.register<JavaExec>("idl_helper_build_project") {
    group = "idl-helper"
    description = "Generate native project"
    mainClass.set(mainClassName)
    args = mutableListOf("gen_desktop_ffm", "gen_desktop_jni", "gen_android_jni", "gen_ios_jni", "gen_teavm")
    classpath = sourceSets["main"].runtimeClasspath
}

tasks.register<JavaExec>("idl_helper_build_project_teavm") {
    group = "idl-helper"
    description = "Generate native project"
    mainClass.set(mainClassName)
    args = mutableListOf("teavm")
    classpath = sourceSets["main"].runtimeClasspath
}

tasks.register<JavaExec>("idl_helper_build_project_windows64_jni") {
    group = "idl-helper"
    description = "Generate native project"
    mainClass.set(mainClassName)
    args = mutableListOf("windows64_jni")
    classpath = sourceSets["main"].runtimeClasspath
}

tasks.register<JavaExec>("idl_helper_build_project_linux64_jni") {
    group = "idl-helper"
    description = "Generate native project"
    mainClass.set(mainClassName)
    args = mutableListOf("linux64_jni")
    classpath = sourceSets["main"].runtimeClasspath
}

tasks.register<JavaExec>("idl_helper_build_project_mac64_jni") {
    group = "idl-helper"
    description = "Generate native project"
    mainClass.set(mainClassName)
    args = mutableListOf("mac64_jni")
    classpath = sourceSets["main"].runtimeClasspath
}

tasks.register<JavaExec>("idl_helper_build_project_macArm_jni") {
    group = "idl-helper"
    description = "Generate native project"
    mainClass.set(mainClassName)
    args = mutableListOf("macArm_jni")
    classpath = sourceSets["main"].runtimeClasspath
}

tasks.register<JavaExec>("idl_helper_build_project_android_jni") {
    group = "idl-helper"
    description = "Generate native project"
    mainClass.set(mainClassName)
    args = mutableListOf("android_jni")
    classpath = sourceSets["main"].runtimeClasspath
}

tasks.register<JavaExec>("idl_helper_build_project_ios_jni") {
    group = "idl-helper"
    description = "Generate native project"
    mainClass.set(mainClassName)
    args = mutableListOf("ios_jni")
    classpath = sourceSets["main"].runtimeClasspath
}

tasks.register<JavaExec>("idl_helper_build_project_windows64_ffm") {
    group = "idl-helper"
    description = "Generate FFM code + compile FFM native for Windows"
    mainClass.set(mainClassName)
    args = mutableListOf("windows64_ffm")
    classpath = sourceSets["main"].runtimeClasspath
}

tasks.register<JavaExec>("idl_helper_build_project_linux64_ffm") {
    group = "idl-helper"
    description = "Generate FFM code + compile FFM native for Linux"
    mainClass.set(mainClassName)
    args = mutableListOf("linux64_ffm")
    classpath = sourceSets["main"].runtimeClasspath
}

tasks.register<JavaExec>("idl_helper_build_project_mac64_ffm") {
    group = "idl-helper"
    description = "Generate FFM code + compile FFM native for macOS"
    mainClass.set(mainClassName)
    args = mutableListOf("mac64_ffm")
    classpath = sourceSets["main"].runtimeClasspath
}

tasks.register<JavaExec>("idl_helper_build_project_macArm_ffm") {
    group = "idl-helper"
    description = "Generate FFM code + compile FFM native for macOS ARM"
    mainClass.set(mainClassName)
    args = mutableListOf("macArm_ffm")
    classpath = sourceSets["main"].runtimeClasspath
}
