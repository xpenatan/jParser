plugins {
    id("java-library")
}

val libDir = "${projectDir}/../../builder/build/c++/libs"
val nativeResourceRoot = "external_cpp/jparser/liba/native/ios"
val gdxTeaVMMarker = resources.text.fromString("ignore-resources=META-INF\n")
val iosBuildTask = ":examples:SharedLib:libA:builder:LibA_build_project_ios_teavm_c"

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
    api(project(":examples:SharedLib:libA:shared:LibA-c"))
}

java {
    sourceCompatibility = JavaVersion.toVersion(LibExt.javaWebTarget)
    targetCompatibility = JavaVersion.toVersion(LibExt.javaWebTarget)
}

tasks.named<Jar>("jar") {
    dependsOn(iosBuildTask)
    iosSlices.forEach { slice ->
        val nativeFile = "$libDir/ios/${slice.sdk}/${slice.architecture}/teavm_c/libLibA64_.a"
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
            "$libDir/ios/${slice.sdk}/${slice.architecture}/teavm_c/libLibA64_.a"
        }.filterNot { file(it).isFile }
        if(missingFiles.isNotEmpty()) {
            throw GradleException(
                "Missing LibA iOS TeaVM C payloads:\n" + missingFiles.joinToString("\n")
            )
        }
    }
}
