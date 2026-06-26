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

tasks.register<JavaExec>("SharedLib_build_app_desktop_c") {
    group = "example-desktop"
    description = "Build SharedLib headless app with TeaVM C"
    dependsOn(
        ":jParser:runtime:plugin:jParser_build_windows64_teavm_c",
        ":examples:SharedLib:libA:plugin:jParser_build_windows64_teavm_c",
        ":examples:SharedLib:libB:plugin:jParser_build_windows64_teavm_c",
        ":examples:SharedLib:libA:lib-c:core:jar",
        ":examples:SharedLib:libB:lib-c:core:jar"
    )
    mainClass.set("BuildTeaVMC")
    classpath = sourceSets["main"].runtimeClasspath
    workingDir = projectDir
}
