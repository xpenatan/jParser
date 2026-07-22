plugins {
    id("java-library")
}

val moduleName = "runtime-ios-c"
val libDir = "${projectDir}/../../builder/build/c++/libs"
val nativeResourceRoot = "external_cpp/jparser/runtime/native/ios"
val gdxTeaVMMarker = resources.text.fromString("ignore-resources=META-INF\n")
val iosBuildTask = ":jParser:runtime:builder:runtime_helper_build_project_ios_teavm_c"

data class IOSNativeSlice(
    val sdk: String,
    val architecture: String,
)

val iosSlices = listOf(
    IOSNativeSlice("device", "arm64"),
    IOSNativeSlice("simulator", "arm64"),
    IOSNativeSlice("simulator", "x86_64"),
)

dependencies {
    api(project(":jParser:runtime:shared:runtime-c"))
}

java {
    sourceCompatibility = JavaVersion.toVersion(LibExt.javaWebTarget)
    targetCompatibility = JavaVersion.toVersion(LibExt.javaWebTarget)
    withJavadocJar()
    withSourcesJar()
}

tasks.named<Jar>("jar") {
    // macOS consumers can build the slices locally. Release aggregation runs
    // on Linux after downloading the CI-built archives, so it must only pack.
    if(System.getProperty("os.name").lowercase().contains("mac")) {
        dependsOn(iosBuildTask)
    }
    iosSlices.forEach { slice ->
        val nativeFile = "$libDir/ios/${slice.sdk}/${slice.architecture}/teavm_c/libruntime64_.a"
        from(nativeFile) {
            into("$nativeResourceRoot/${slice.sdk}/${slice.architecture}")
        }
    }
    from(gdxTeaVMMarker.asFile()) {
        into("META-INF")
        rename { "gdx-teavm.properties" }
    }
    doFirst {
        val missingFiles = iosSlices.map { slice ->
            "$libDir/ios/${slice.sdk}/${slice.architecture}/teavm_c/libruntime64_.a"
        }.filterNot { file(it).isFile }
        if(missingFiles.isNotEmpty()) {
            throw GradleException(
                "Missing iOS TeaVM C runtime payloads:\n" + missingFiles.joinToString("\n")
            )
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifactId = moduleName
            groupId = LibExt.groupId
            version = LibExt.libVersion
            from(components["java"])
        }
    }
}
