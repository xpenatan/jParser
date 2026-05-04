plugins {
    id("java")
}

val mainClassName = "BuildLib"

dependencies {
    implementation(project(":examples:TestLib:lib:lib-base"))
    implementation(project(":jParser:gen:gen-core"))
    implementation(project(":jParser:gen:gen-idl"))
    implementation(project(":jParser:gen:gen-web"))
    implementation(project(":jParser:gen:gen-jni"))
    implementation(project(":jParser:gen:gen-ffm"))
    implementation(project(":jParser:gen:gen-build"))
    implementation(project(":jParser:gen:gen-build-tool"))
    implementation(project(":jParser:runtime:runtime-core"))
}

tasks.register<JavaExec>("TestLib_build_project") {
    group = "lib"
    description = "Generate native project"
    mainClass.set(mainClassName)
    args = mutableListOf("gen_jni", "gen_ffm", "gen_web")
    classpath = sourceSets["main"].runtimeClasspath
}

tasks.register<JavaExec>("TestLib_build_project_web_wasm") {
    group = "lib"
    description = "Generate native project"
    mainClass.set(mainClassName)
    args = mutableListOf("gen_web", "web_wasm")
    classpath = sourceSets["main"].runtimeClasspath
}

tasks.register<JavaExec>("TestLib_build_project_windows64_ffm") {
    group = "lib"
    description = "Generate FFM Java code and compile for Windows with FFMGlue"
    mainClass.set(mainClassName)
    args = mutableListOf("gen_ffm", "windows64_ffm")
    classpath = sourceSets["main"].runtimeClasspath
}

tasks.register<JavaExec>("TestLib_build_project_linux64_ffm") {
    group = "lib"
    description = "Generate FFM Java code and compile for Linux with FFMGlue"
    mainClass.set(mainClassName)
    args = mutableListOf("gen_ffm", "linux64_ffm")
    classpath = sourceSets["main"].runtimeClasspath
}

tasks.register<JavaExec>("TestLib_build_project_mac64_ffm") {
    group = "lib"
    description = "Generate FFM Java code and compile for Mac with FFMGlue"
    mainClass.set(mainClassName)
    args = mutableListOf("gen_ffm", "mac64_ffm")
    classpath = sourceSets["main"].runtimeClasspath
}

tasks.register<JavaExec>("TestLib_build_project_macArm_ffm") {
    group = "lib"
    description = "Generate FFM Java code and compile for Mac ARM with FFMGlue"
    mainClass.set(mainClassName)
    args = mutableListOf("gen_ffm", "macArm_ffm")
    classpath = sourceSets["main"].runtimeClasspath
}

tasks.register<JavaExec>("TestLib_build_project_windows64_jni") {
    group = "lib"
    description = "Generate native project"
    mainClass.set(mainClassName)
    args = mutableListOf("gen_jni", "windows64_jni")
    classpath = sourceSets["main"].runtimeClasspath
}

tasks.register<JavaExec>("TestLib_build_project_linux64_jni") {
    group = "lib"
    description = "Generate native project"
    mainClass.set(mainClassName)
    args = mutableListOf("gen_jni", "linux64_jni")
    classpath = sourceSets["main"].runtimeClasspath
}

tasks.register<JavaExec>("TestLib_build_project_mac64_jni") {
    group = "lib"
    description = "Generate native project"
    mainClass.set(mainClassName)
    args = mutableListOf("gen_jni", "mac64_jni")
    classpath = sourceSets["main"].runtimeClasspath
}

tasks.register<JavaExec>("TestLib_build_project_macArm_jni") {
    group = "lib"
    description = "Generate native project"
    mainClass.set(mainClassName)
    args = mutableListOf("gen_jni", "macArm_jni")
    classpath = sourceSets["main"].runtimeClasspath
}

tasks.register<JavaExec>("TestLib_build_project_android_jni") {
    group = "lib"
    description = "Generate native project"
    mainClass.set(mainClassName)
    args = mutableListOf("gen_jni", "android_jni")
    classpath = sourceSets["main"].runtimeClasspath
}

tasks.register<JavaExec>("TestLib_build_project_ios_jni") {
    group = "lib"
    description = "Generate native project"
    mainClass.set(mainClassName)
    args = mutableListOf("gen_jni", "ios_jni")
    classpath = sourceSets["main"].runtimeClasspath
}

