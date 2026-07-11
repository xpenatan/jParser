plugins {
    id("base")
}

val moduleName = "runtime-desktop-c"

val libDir = "${projectDir}/../../builder/build/c++/libs"
val nativeResourceRoot = "external_cpp/jparser/runtime/native"
val gdxTeaVMMarker = resources.text.fromString("ignore-resources=META-INF\n")

val platforms: Map<String, List<String>> = mapOf(
    "windows_x64" to listOf(
        "$libDir/windows/vc/teavm_c/runtime64_.lib",
        "$libDir/windows/vc/teavm_c/runtime64.lib",
        "$libDir/windows/vc/teavm_c/runtime64.dll",
    ),
    "linux_x64" to listOf(
        "$libDir/linux/teavm_c/libruntime64_.a",
        "$libDir/linux/teavm_c/libruntime64.so",
    ),
    "mac_x64" to listOf(
        "$libDir/mac/teavm_c/libruntime64_.a",
        "$libDir/mac/teavm_c/libruntime64.dylib",
    ),
    "mac_arm64" to listOf(
        "$libDir/mac/arm/teavm_c/libruntime64_.a",
        "$libDir/mac/arm/teavm_c/libruntimearm64.dylib",
    ),
)

val nativeJars = platforms.map { (platform, nativeFiles) ->
    platform to tasks.register<Jar>("nativeJar_${platform}") {
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
                groupId = LibExt.groupId
                version = LibExt.libVersion
                artifact(nativeJar)
            }
        }
    }
}
