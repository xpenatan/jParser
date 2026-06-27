plugins {
    id("java")
}

java {
    sourceCompatibility = JavaVersion.toVersion(LibExt.javaWebTarget)
    targetCompatibility = JavaVersion.toVersion(LibExt.javaWebTarget)
}

dependencies {
    implementation(project(":examples:SharedLib:app:core"))
    implementation(project(":examples:SharedLib:libA:lib-c:core"))
    implementation(project(":examples:SharedLib:libB:lib-c:core"))
    implementation(project(":jParser:runtime:runtime-c:core"))

    implementation("org.teavm:teavm-tooling:${LibExt.teaVMVersion}")
    implementation("org.teavm:teavm-classlib:${LibExt.teaVMVersion}")
}

val runtimeTeaVMCBuildTask = LibExt.hostBuildProjectTask(":jParser:runtime:runtime-build", "runtime_helper", "teavm_c")
val libATeaVMCBuildTask = LibExt.hostBuildProjectTask(":examples:SharedLib:libA:lib-build", "LibA", "teavm_c")
val libBTeaVMCBuildTask = LibExt.hostBuildProjectTask(":examples:SharedLib:libB:lib-build", "LibB", "teavm_c")

tasks.register<JavaExec>("SharedLib_build_app_desktop_c") {
    group = "example-desktop"
    description = "Build SharedLib headless app with TeaVM C"
    dependsOn(
        runtimeTeaVMCBuildTask,
        libATeaVMCBuildTask,
        libBTeaVMCBuildTask,
        ":examples:SharedLib:libA:lib-c:core:jar",
        ":examples:SharedLib:libB:lib-c:core:jar"
    )
    mainClass.set("BuildTeaVMC")
    classpath = sourceSets["main"].runtimeClasspath
    workingDir = projectDir
}
