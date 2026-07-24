plugins {
    id("java")
}

java {
    sourceCompatibility = JavaVersion.toVersion(libs.versions.javaWeb.get())
    targetCompatibility = JavaVersion.toVersion(libs.versions.javaWeb.get())
}

dependencies {
    implementation(project(":examples:TestLib:app:core"))
    implementation(project(":examples:TestLib:lib:shared:TestLib-c"))
    implementation(project(":jParser:runtime:shared:runtime-c"))

    implementation(libs.bundles.teavmCompiler)
}

val hostOs = System.getProperty("os.name")
val hostArch = System.getProperty("os.arch")
val hostTarget = when {
    hostOs.startsWith("Windows") -> "windows64"
    hostOs == "Linux" && (hostArch == "x86_64" || hostArch == "amd64") -> "linux64"
    hostOs.startsWith("Mac") && (hostArch == "aarch64" || hostArch == "arm64") -> "macArm"
    hostOs.startsWith("Mac") && (hostArch == "x86_64" || hostArch == "amd64") -> "mac64"
    else -> error("Unsupported desktop host: os=$hostOs arch=$hostArch")
}

val runtimeTeaVMCBuildTask = ":jParser:runtime:builder:runtime_helper_build_project_${hostTarget}_teavm_c"
val testLibTeaVMCBuildTask = ":examples:TestLib:lib:builder:TestLib_build_project_${hostTarget}_teavm_c"

tasks.register<JavaExec>("TestLib_build_app_desktop_c") {
    group = "example-desktop"
    description = "Build TestLib headless app with TeaVM C"
    dependsOn(
        runtimeTeaVMCBuildTask,
        testLibTeaVMCBuildTask,
        ":examples:TestLib:lib:shared:TestLib-c:jar"
    )
    mainClass.set("BuildTeaVMC")
    classpath = sourceSets["main"].runtimeClasspath
    workingDir = projectDir
}
