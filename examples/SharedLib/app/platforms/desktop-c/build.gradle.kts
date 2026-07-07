plugins {
    id("java")
}

java {
    sourceCompatibility = JavaVersion.toVersion(LibExt.javaWebTarget)
    targetCompatibility = JavaVersion.toVersion(LibExt.javaWebTarget)
}

dependencies {
    implementation(project(":examples:SharedLib:app:core"))
    implementation(project(":examples:SharedLib:libA:shared:LibA-c"))
    implementation(project(":examples:SharedLib:libB:shared:LibB-c"))
    implementation(project(":jParser:runtime:shared:runtime-c"))

    implementation("org.teavm:teavm-tooling:${LibExt.teaVMVersion}")
    implementation("org.teavm:teavm-classlib:${LibExt.teaVMVersion}")
}

val runtimeTeaVMCBuildTask = LibExt.hostBuildProjectTask(":jParser:runtime:builder", "runtime_helper", "teavm_c")
val libATeaVMCBuildTask = LibExt.hostBuildProjectTask(":examples:SharedLib:libA:builder", "LibA", "teavm_c")
val libBTeaVMCBuildTask = LibExt.hostBuildProjectTask(":examples:SharedLib:libB:builder", "LibB", "teavm_c")

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
