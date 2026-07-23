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

val runtimeTeaVMCBuildTask = JParserBuildTasks.hostBuildProjectTask(":jParser:runtime:builder", "runtime_helper", "teavm_c")
val testLibTeaVMCBuildTask = JParserBuildTasks.hostBuildProjectTask(":examples:TestLib:lib:builder", "TestLib", "teavm_c")

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
