plugins {
    id("base")
}

val moduleName = "TestLib-desktop-c"

val libDir = "${projectDir}/../../builder/build/c++/libs"
val nativeResourceRoot = "external_cpp/jparser/testlib/native"
val gdxTeaVMMarker = resources.text.fromString("ignore-resources=META-INF\n")

val platforms: Map<String, List<String>> = mapOf(
    "windows_x64" to listOf(
        "$libDir/windows/vc/teavm_c/TestLib64_.lib",
        "$libDir/windows/vc/teavm_c/TestLib64.lib",
        "$libDir/windows/vc/teavm_c/TestLib64.dll",
    ),
    "linux_x64" to listOf(
        "$libDir/linux/teavm_c/libTestLib64_.a",
        "$libDir/linux/teavm_c/libTestLib64.so",
    ),
    "mac_x64" to listOf(
        "$libDir/mac/teavm_c/libTestLib64_.a",
        "$libDir/mac/teavm_c/libTestLib64.dylib",
    ),
    "mac_arm64" to listOf(
        "$libDir/mac/arm/teavm_c/libTestLib64_.a",
        "$libDir/mac/arm/teavm_c/libTestLibarm64.dylib",
    ),
)

platforms.forEach { (platform, nativeFiles) ->
    tasks.register<Jar>("nativeJar_${platform}") {
        nativeFiles.forEach { nativeFile ->
            from(nativeFile) {
                into("$nativeResourceRoot/$platform")
            }
        }
        from(gdxTeaVMMarker.asFile()) {
            into("META-INF")
            rename { "gdx-teavm.properties" }
        }
        archiveBaseName.set("${moduleName}-${platform}")
        archiveClassifier.set("")
        doFirst {
            val missingFiles = nativeFiles.filterNot { file(it).isFile }
            if(missingFiles.isNotEmpty()) {
                throw GradleException(
                    "Missing TestLib TeaVM C native payloads for $platform:\n" +
                        missingFiles.joinToString("\n")
                )
            }
        }
    }
}
