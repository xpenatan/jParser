plugins {
    id("java")
}

val mainClassName = "BuildLib"
fun jParserGenerator(name: String) = providers.provider {
    "${libs.versions.jParserGroup.get()}:$name:${libs.versions.jParserSnapshot.get()}"
}

dependencies {
    implementation(project(":examples:TestLib:lib:base"))
    implementation(jParserGenerator("gen-core"))
    implementation(jParserGenerator("gen-idl"))
    implementation(jParserGenerator("gen-web"))
    implementation(jParserGenerator("gen-c"))
    implementation(jParserGenerator("gen-jni"))
    implementation(jParserGenerator("gen-ffm"))
    implementation(jParserGenerator("gen-build"))
    implementation(jParserGenerator("gen-build-tool"))
    implementation(project(":jParser:runtime:core"))
}

tasks.register<JavaExec>("TestLib_build_project") {
    group = "lib"
    description = "Generate native project"
    mainClass.set(mainClassName)
    args = mutableListOf("gen_jni", "gen_ffm", "gen_web", "gen_teavm_c")
    classpath = sourceSets["main"].runtimeClasspath
}

tasks.register<JavaExec>("TestLib_build_project_web_wasm") {
    group = "lib"
    description = "Generate native project"
    mainClass.set(mainClassName)
    args = mutableListOf("gen_web", "web_wasm")
    classpath = sourceSets["main"].runtimeClasspath
}

tasks.register<JavaExec>("TestLib_build_project_windows64_teavm_c") {
    group = "lib"
    description = "Generate TeaVM C Java bindings and compile native library for Windows"
    mainClass.set(mainClassName)
    args = mutableListOf("gen_teavm_c", "windows64_teavm_c")
    classpath = sourceSets["main"].runtimeClasspath
}

tasks.register<JavaExec>("TestLib_build_project_linux64_teavm_c") {
    group = "lib"
    description = "Generate TeaVM C Java bindings and compile native library for Linux"
    mainClass.set(mainClassName)
    args = mutableListOf("gen_teavm_c", "linux64_teavm_c")
    classpath = sourceSets["main"].runtimeClasspath
}

tasks.register<JavaExec>("TestLib_build_project_mac64_teavm_c") {
    group = "lib"
    description = "Generate TeaVM C Java bindings and compile native library for macOS"
    mainClass.set(mainClassName)
    args = mutableListOf("gen_teavm_c", "mac64_teavm_c")
    classpath = sourceSets["main"].runtimeClasspath
}

tasks.register<JavaExec>("TestLib_build_project_macArm_teavm_c") {
    group = "lib"
    description = "Generate TeaVM C Java bindings and compile native library for macOS ARM"
    mainClass.set(mainClassName)
    args = mutableListOf("gen_teavm_c", "macArm_teavm_c")
    classpath = sourceSets["main"].runtimeClasspath
}

tasks.register<JavaExec>("TestLib_build_project_android_teavm_c") {
    group = "lib"
    description = "Generate TeaVM C Java bindings and compile native library for Android"
    mainClass.set(mainClassName)
    args = mutableListOf("gen_teavm_c", "android_teavm_c")
    classpath = sourceSets["main"].runtimeClasspath
}

tasks.register<JavaExec>("TestLib_build_project_ios_teavm_c") {
    group = "lib"
    description = "Generate TeaVM C Java bindings and compile native library for iOS"
    mainClass.set(mainClassName)
    args = mutableListOf("gen_teavm_c", "ios_teavm_c")
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
