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
    args = mutableListOf()
    classpath = sourceSets["main"].runtimeClasspath
}

tasks.register<JavaExec>("idl_helper_build_project_teavm") {
    group = "idl-helper"
    description = "Generate native project"
    mainClass.set(mainClassName)
    args = mutableListOf("teavm")
    classpath = sourceSets["main"].runtimeClasspath
}

tasks.register<JavaExec>("idl_helper_build_project_jni_windows64") {
    group = "idl-helper"
    description = "Generate native project"
    mainClass.set(mainClassName)
    args = mutableListOf("jni_windows64")
    classpath = sourceSets["main"].runtimeClasspath
}

tasks.register<JavaExec>("idl_helper_build_project_jni_linux64") {
    group = "idl-helper"
    description = "Generate native project"
    mainClass.set(mainClassName)
    args = mutableListOf("jni_linux64")
    classpath = sourceSets["main"].runtimeClasspath
}

tasks.register<JavaExec>("idl_helper_build_project_jni_mac64") {
    group = "idl-helper"
    description = "Generate native project"
    mainClass.set(mainClassName)
    args = mutableListOf("jni_mac64")
    classpath = sourceSets["main"].runtimeClasspath
}

tasks.register<JavaExec>("idl_helper_build_project_jni_macArm") {
    group = "idl-helper"
    description = "Generate native project"
    mainClass.set(mainClassName)
    args = mutableListOf("jni_macArm")
    classpath = sourceSets["main"].runtimeClasspath
}

tasks.register<JavaExec>("idl_helper_build_project_jni_android") {
    group = "idl-helper"
    description = "Generate native project"
    mainClass.set(mainClassName)
    args = mutableListOf("jni_android")
    classpath = sourceSets["main"].runtimeClasspath
}

tasks.register<JavaExec>("idl_helper_build_project_jni_ios") {
    group = "idl-helper"
    description = "Generate native project"
    mainClass.set(mainClassName)
    args = mutableListOf("jni_ios")
    classpath = sourceSets["main"].runtimeClasspath
}

tasks.register<JavaExec>("idl_helper_build_project_ffm_windows64") {
    group = "idl-helper"
    description = "Generate FFM code + compile FFM native for Windows"
    mainClass.set(mainClassName)
    args = mutableListOf("ffm_windows64")
    classpath = sourceSets["main"].runtimeClasspath
}

tasks.register<JavaExec>("idl_helper_build_project_ffm_linux64") {
    group = "idl-helper"
    description = "Generate FFM code + compile FFM native for Linux"
    mainClass.set(mainClassName)
    args = mutableListOf("ffm_linux64")
    classpath = sourceSets["main"].runtimeClasspath
}

tasks.register<JavaExec>("idl_helper_build_project_ffm_mac64") {
    group = "idl-helper"
    description = "Generate FFM code + compile FFM native for macOS"
    mainClass.set(mainClassName)
    args = mutableListOf("ffm_mac64")
    classpath = sourceSets["main"].runtimeClasspath
}

tasks.register<JavaExec>("idl_helper_build_project_ffm_macArm") {
    group = "idl-helper"
    description = "Generate FFM code + compile FFM native for macOS ARM"
    mainClass.set(mainClassName)
    args = mutableListOf("ffm_macArm")
    classpath = sourceSets["main"].runtimeClasspath
}
