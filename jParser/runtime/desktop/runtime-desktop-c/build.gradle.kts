plugins {
    id("base")
}

val moduleName = "runtime-desktop-c"

val libDir = "${projectDir}/../../builder/build/c++/libs"
val nativeResourceRoot = "external_cpp/jparser/runtime/native"
val gdxTeaVMMarker = resources.text.fromString("ignore-resources=META-INF\n")

val platforms: Map<String, List<Pair<String, String>>> = mapOf(
    "windows_x64" to listOf(
        "$libDir/mt/windows/vc/teavm_c/runtime64_.lib" to "$nativeResourceRoot/windows_x64/mt/static",
        "$libDir/mt/windows/vc/teavm_c/runtime64.lib" to "$nativeResourceRoot/windows_x64/mt/shared",
        "$libDir/mt/windows/vc/teavm_c/runtime64.dll" to "$nativeResourceRoot/windows_x64/mt/shared",
        "$libDir/md/windows/vc/teavm_c/runtime64_.lib" to "$nativeResourceRoot/windows_x64/md/static",
        "$libDir/md/windows/vc/teavm_c/runtime64.lib" to "$nativeResourceRoot/windows_x64/md/shared",
        "$libDir/md/windows/vc/teavm_c/runtime64.dll" to "$nativeResourceRoot/windows_x64/md/shared",
        // Preserve the pre-runtime-selection layout as an MT compatibility fallback.
        "$libDir/mt/windows/vc/teavm_c/runtime64_.lib" to "$nativeResourceRoot/windows_x64",
        "$libDir/mt/windows/vc/teavm_c/runtime64.lib" to "$nativeResourceRoot/windows_x64",
        "$libDir/mt/windows/vc/teavm_c/runtime64.dll" to "$nativeResourceRoot/windows_x64",
    ),
    "linux_x64" to listOf(
        "$libDir/linux/teavm_c/libruntime64_.a" to "$nativeResourceRoot/linux_x64",
        "$libDir/linux/teavm_c/libruntime64.so" to "$nativeResourceRoot/linux_x64",
    ),
    "mac_x64" to listOf(
        "$libDir/mac/teavm_c/libruntime64_.a" to "$nativeResourceRoot/mac_x64",
        "$libDir/mac/teavm_c/libruntime64.dylib" to "$nativeResourceRoot/mac_x64",
    ),
    "mac_arm64" to listOf(
        "$libDir/mac/arm/teavm_c/libruntime64_.a" to "$nativeResourceRoot/mac_arm64",
        "$libDir/mac/arm/teavm_c/libruntimearm64.dylib" to "$nativeResourceRoot/mac_arm64",
    ),
)

val nativeJars = platforms.map { (platform, nativePayloads) ->
    platform to tasks.register<Jar>("nativeJar_${platform}") {
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
                    "Missing desktop TeaVM C native payloads for $platform:\n" +
                        missingFiles.joinToString("\n")
                )
            }
        }
    }
}

val nativeRuntime by configurations.creating {
    isCanBeConsumed = true
    isCanBeResolved = false
}

artifacts {
    nativeJars.forEach { add(nativeRuntime.name, it.second) }
}

publishing {
    publications {
        nativeJars.forEach { (platform, nativeJar) ->
            create<MavenPublication>("mavenNative_${platform}") {
                artifactId = "${moduleName}_${platform}"
                groupId = project.group.toString()
                version = project.version.toString()
                artifact(nativeJar)
            }
        }
    }
}
