plugins {
    id("java")
}

java {
    sourceCompatibility = JavaVersion.toVersion(LibExt.java11Target)
    targetCompatibility = JavaVersion.toVersion(LibExt.java11Target)
}

val mainClassName = "BuildLib"

dependencies {
    implementation(project(":examples:TestLib:lib:lib-base"))
    implementation(project(":jParser:jParser-core"))
    implementation(project(":jParser:jParser-idl"))
    implementation(project(":jParser:jParser-teavm"))
    implementation(project(":jParser:jParser-jni"))
    implementation(project(":jParser:jParser-build"))
    implementation(project(":jParser:jParser-build-tool"))
    implementation(project(":idl-helper:idl-helper-core"))
}

tasks.register<JavaExec>("TestLib_build_project") {
    group = "lib"
    description = "Generate native project"
    mainClass.set(mainClassName)
    args = mutableListOf()
    classpath = sourceSets["main"].runtimeClasspath
}

tasks.register<JavaExec>("TestLib_build_project_teavm") {
    group = "lib"
    description = "Generate native project"
    mainClass.set(mainClassName)
    args = mutableListOf("teavm")
    classpath = sourceSets["main"].runtimeClasspath
}

tasks.register<JavaExec>("TestLib_build_project_ffm_windows64") {
    group = "lib"
    description = "Generate FFM Java code and compile for Windows with FFMGlue"
    mainClass.set(mainClassName)
    args = mutableListOf("ffm_windows64")
    classpath = sourceSets["main"].runtimeClasspath
}

tasks.register<JavaExec>("TestLib_build_project_ffm_linux64") {
    group = "lib"
    description = "Generate FFM Java code and compile for Linux with FFMGlue"
    mainClass.set(mainClassName)
    args = mutableListOf("ffm_linux64")
    classpath = sourceSets["main"].runtimeClasspath
}

tasks.register<JavaExec>("TestLib_build_project_ffm_mac64") {
    group = "lib"
    description = "Generate FFM Java code and compile for Mac with FFMGlue"
    mainClass.set(mainClassName)
    args = mutableListOf("ffm_mac64")
    classpath = sourceSets["main"].runtimeClasspath
}

tasks.register<JavaExec>("TestLib_build_project_ffm_macArm") {
    group = "lib"
    description = "Generate FFM Java code and compile for Mac ARM with FFMGlue"
    mainClass.set(mainClassName)
    args = mutableListOf("ffm_macArm")
    classpath = sourceSets["main"].runtimeClasspath
}

tasks.register<JavaExec>("TestLib_build_project_jni_windows64") {
    group = "lib"
    description = "Generate native project"
    mainClass.set(mainClassName)
    args = mutableListOf("jni_windows64")
    classpath = sourceSets["main"].runtimeClasspath
}

tasks.register<JavaExec>("TestLib_build_project_jni_linux64") {
    group = "lib"
    description = "Generate native project"
    mainClass.set(mainClassName)
    args = mutableListOf("jni_linux64")
    classpath = sourceSets["main"].runtimeClasspath
}

tasks.register<JavaExec>("TestLib_build_project_jni_mac64") {
    group = "lib"
    description = "Generate native project"
    mainClass.set(mainClassName)
    args = mutableListOf("jni_mac64")
    classpath = sourceSets["main"].runtimeClasspath
}

tasks.register<JavaExec>("TestLib_build_project_jni_macArm") {
    group = "lib"
    description = "Generate native project"
    mainClass.set(mainClassName)
    args = mutableListOf("jni_macArm")
    classpath = sourceSets["main"].runtimeClasspath
}

tasks.register<JavaExec>("TestLib_build_project_jni_android") {
    group = "lib"
    description = "Generate native project"
    mainClass.set(mainClassName)
    args = mutableListOf("jni_android")
    classpath = sourceSets["main"].runtimeClasspath
}

tasks.register<JavaExec>("TestLib_build_project_jni_ios") {
    group = "lib"
    description = "Generate native project"
    mainClass.set(mainClassName)
    args = mutableListOf("jni_ios")
    classpath = sourceSets["main"].runtimeClasspath
}

