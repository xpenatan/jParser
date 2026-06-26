plugins {
    id("java")
}

java {
    sourceCompatibility = JavaVersion.toVersion(LibExt.javaWebTarget)
    targetCompatibility = JavaVersion.toVersion(LibExt.javaWebTarget)
}

dependencies {
    implementation(project(":examples:TestLib:app:core"))
    implementation(project(":examples:TestLib:lib:lib-c:core"))
    implementation(project(":jParser:runtime:runtime-c:core"))

    implementation("org.teavm:teavm-tooling:${LibExt.teaVMVersion}")
    implementation("org.teavm:teavm-classlib:${LibExt.teaVMVersion}")
}

val runtimeTeaVMCBuildTask = LibExt.hostBuildProjectTask(":jParser:runtime:runtime-build", "runtime_helper", "teavm_c")
val testLibTeaVMCBuildTask = LibExt.hostBuildProjectTask(":examples:TestLib:lib:lib-build", "TestLib", "teavm_c")

tasks.register<JavaExec>("TestLib_build_app_desktop_c") {
    group = "example-desktop"
    description = "Build TestLib headless app with TeaVM C"
    dependsOn(
        runtimeTeaVMCBuildTask,
        testLibTeaVMCBuildTask,
        ":examples:TestLib:lib:lib-c:core:jar"
    )
    mainClass.set("BuildTeaVMC")
    classpath = sourceSets["main"].runtimeClasspath
    workingDir = projectDir
}
