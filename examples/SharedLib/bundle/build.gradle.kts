plugins {
    java
}

dependencies {
    implementation(project(":jParser:gen:gen-build"))
    implementation(project(":jParser:gen:gen-build-tool"))
}

java {
    sourceCompatibility = JavaVersion.toVersion(libs.versions.javaFfm.get())
    targetCompatibility = JavaVersion.toVersion(libs.versions.javaFfm.get())
}

val hostOs = System.getProperty("os.name")
val hostArch = System.getProperty("os.arch")
val hostTarget = when {
    hostOs.startsWith("Windows") -> "windows64"
    hostOs == "Linux" && (hostArch == "x86_64" || hostArch == "amd64") -> "linux64"
    hostOs.startsWith("Mac") && (hostArch == "aarch64" || hostArch == "arm64") -> "macArm"
    hostOs.startsWith("Mac") && (hostArch == "x86_64" || hostArch == "amd64") -> "mac64"
    else -> error("Unsupported SharedLib fat-bundle host: os=$hostOs arch=$hostArch")
}

fun registerBundleTask(
    taskName: String,
    mode: String,
    runtimeBridge: String,
    libABridge: String,
    libBBridge: String
) = tasks.register<JavaExec>(taskName) {
    group = "verification"
    description = "Build and inspect the SharedLib $mode fat native bundle."
    dependsOn(
        "classes",
        ":jParser:runtime:builder:runtime_helper_build_project_${hostTarget}_$runtimeBridge",
        ":examples:SharedLib:libA:builder:LibA_build_project_${hostTarget}_$libABridge",
        ":examples:SharedLib:libB:builder:LibB_build_project_${hostTarget}_$libBBridge"
    )
    if(libBBridge == "ffm" && libABridge != "ffm") {
        // LibB's preserved standalone output still links to LibA's FFM DLL.
        dependsOn(
            ":examples:SharedLib:libA:builder:LibA_build_project_${hostTarget}_ffm"
        )
    }
    mainClass.set("BuildSharedLibBundle")
    classpath = sourceSets["main"].runtimeClasspath
    val output = layout.buildDirectory.dir("native/$mode")
    outputs.dir(output)
    outputs.upToDateWhen { false }
    doFirst {
        args = listOf(
            rootProject.projectDir.absolutePath,
            mode,
            output.get().asFile.absolutePath
        )
    }
}

registerBundleTask(
    "SharedLib_build_bundle_desktop_jni",
    "jni",
    "jni",
    "jni",
    "jni"
)
registerBundleTask(
    "SharedLib_build_bundle_desktop_mixed",
    "mixed",
    "ffm",
    "jni",
    "ffm"
)
