plugins {
    id("base")
}

val moduleName = "TestLib-desktop-c"

val libDir = "${projectDir}/../../builder/build/c++/libs"
val nativeResourceRoot = "external_cpp/jparser/testlib/native"
val gdxTeaVMMarker = resources.text.fromString("ignore-resources=META-INF\n")

val platforms: Map<String, List<Pair<String, String>>> = mapOf(
    "windows_x64" to listOf(
        "$libDir/mt/windows/vc/teavm_c/TestLib64_.lib" to "$nativeResourceRoot/windows_x64/mt/static",
        "$libDir/mt/windows/vc/teavm_c/TestLib64.lib" to "$nativeResourceRoot/windows_x64/mt/shared",
        "$libDir/mt/windows/vc/teavm_c/TestLib64.dll" to "$nativeResourceRoot/windows_x64/mt/shared",
        "$libDir/md/windows/vc/teavm_c/TestLib64_.lib" to "$nativeResourceRoot/windows_x64/md/static",
        "$libDir/md/windows/vc/teavm_c/TestLib64.lib" to "$nativeResourceRoot/windows_x64/md/shared",
        "$libDir/md/windows/vc/teavm_c/TestLib64.dll" to "$nativeResourceRoot/windows_x64/md/shared",
    ),
    "linux_x64" to listOf(
        "$libDir/linux/teavm_c/libTestLib64_.a" to "$nativeResourceRoot/linux_x64",
        "$libDir/linux/teavm_c/libTestLib64.so" to "$nativeResourceRoot/linux_x64",
    ),
    "mac_x64" to listOf(
        "$libDir/mac/teavm_c/libTestLib64_.a" to "$nativeResourceRoot/mac_x64",
        "$libDir/mac/teavm_c/libTestLib64.dylib" to "$nativeResourceRoot/mac_x64",
    ),
    "mac_arm64" to listOf(
        "$libDir/mac/arm/teavm_c/libTestLib64_.a" to "$nativeResourceRoot/mac_arm64",
        "$libDir/mac/arm/teavm_c/libTestLibarm64.dylib" to "$nativeResourceRoot/mac_arm64",
    ),
)

platforms.forEach { (platform, nativePayloads) ->
    tasks.register<Jar>("nativeJar_${platform}") {
        nativePayloads.forEach { (nativeFile, resourceDirectory) ->
            from(nativeFile) {
                into(resourceDirectory)
            }
        }
        from(gdxTeaVMMarker.asFile()) {
            into("META-INF")
            rename { "gdx-teavm.properties" }
        }
        archiveBaseName.set("${moduleName}-${platform}")
        archiveClassifier.set("")
        doFirst {
            val missingFiles = nativePayloads.map { it.first }.distinct().filterNot { file(it).isFile }
            if(missingFiles.isNotEmpty()) {
                throw GradleException(
                    "Missing TestLib TeaVM C native payloads for $platform:\n" +
                        missingFiles.joinToString("\n")
                )
            }
        }
    }
}
