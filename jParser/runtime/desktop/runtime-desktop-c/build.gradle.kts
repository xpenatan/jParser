plugins {
    id("base")
}

val moduleName = "runtime-desktop-c"

val libDir = "${projectDir}/../../builder/build/c++/libs"
val windowsFile = "$libDir/windows/vc/teavm_c/runtime64_.lib"
val linuxFile = "$libDir/linux/teavm_c/libruntime64_.a"
val macFile = "$libDir/mac/teavm_c/libruntime64_.a"
val macArmFile = "$libDir/mac/arm/teavm_c/libruntime64_.a"
val nativeResourceRoot = "external_cpp/jparser/runtime/native"
val gdxTeaVMMarker = resources.text.fromString("ignore-resources=META-INF\n")

val platforms: Map<String, String> = mapOf(
    "windows_x64" to windowsFile,
    "linux_x64" to linuxFile,
    "mac_x64" to macFile,
    "mac_arm64" to macArmFile,
)

val nativeJars = platforms.map { (platform, nativeFile) ->
    platform to tasks.register<Jar>("nativeJar_${platform}") {
        from(nativeFile) {
            into("$nativeResourceRoot/$platform")
        }
        from(gdxTeaVMMarker.asFile()) {
            into("META-INF")
            rename { "gdx-teavm.properties" }
        }
        archiveBaseName.set("${moduleName}-${platform}")
        archiveClassifier.set("")
        doFirst {
            if(!file(nativeFile).isFile) {
                logger.warn("Missing desktop TeaVM C static archive for $platform: $nativeFile")
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
