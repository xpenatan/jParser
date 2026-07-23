plugins {
    id("java")
}

java {
    sourceCompatibility = JavaVersion.toVersion(libs.versions.javaWeb.get())
    targetCompatibility = JavaVersion.toVersion(libs.versions.javaWeb.get())
}

dependencies {
    implementation(project(":examples:SharedLib:app:core"))
    implementation(project(":examples:SharedLib:libA:shared:LibA-c"))
    implementation(project(":examples:SharedLib:libB:shared:LibB-c"))
    implementation(project(":jParser:runtime:shared:runtime-c"))

    implementation(libs.bundles.teavmCompiler)
}

val runtimeTeaVMCBuildTask = JParserBuildTasks.hostBuildProjectTask(":jParser:runtime:builder", "runtime_helper", "teavm_c")
val libATeaVMCBuildTask = JParserBuildTasks.hostBuildProjectTask(":examples:SharedLib:libA:builder", "LibA", "teavm_c")
val libBTeaVMCBuildTask = JParserBuildTasks.hostBuildProjectTask(":examples:SharedLib:libB:builder", "LibB", "teavm_c")

tasks.register<JavaExec>("SharedLib_build_app_desktop_c") {
    group = "example-desktop"
    description = "Build SharedLib headless app with TeaVM C"
    dependsOn(
        runtimeTeaVMCBuildTask,
        libATeaVMCBuildTask,
        libBTeaVMCBuildTask,
        ":examples:SharedLib:libA:shared:LibA-c:jar",
        ":examples:SharedLib:libB:shared:LibB-c:jar"
    )
    mainClass.set("BuildTeaVMC")
    classpath = sourceSets["main"].runtimeClasspath
    workingDir = projectDir
}
