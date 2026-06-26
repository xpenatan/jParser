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

tasks.register<JavaExec>("TestLib_build_app_desktop_c") {
    group = "example-desktop"
    description = "Build TestLib headless app with TeaVM C"
    dependsOn(
        ":jParser:runtime:plugin:jParser_build_windows64_teavm_c",
        ":examples:TestLib:lib:plugin:jParser_build_windows64_teavm_c",
        ":examples:TestLib:lib:lib-c:core:jar"
    )
    mainClass.set("BuildTeaVMC")
    classpath = sourceSets["main"].runtimeClasspath
    workingDir = projectDir
}
