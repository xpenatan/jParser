plugins {
    id("base")
}

val moduleName = "LibA-desktop-c"

val libDir = "${projectDir}/../../builder/build/c++/libs"
val windowsFile = "$libDir/windows/vc/teavm_c/LibA64_.lib"
val linuxFile = "$libDir/linux/teavm_c/libLibA64_.a"
val macFile = "$libDir/mac/teavm_c/libLibA64_.a"
val macArmFile = "$libDir/mac/arm/teavm_c/libLibA64_.a"
val nativeResourceRoot = "external_cpp/jparser/liba/native"
val gdxTeaVMMarker = resources.text.fromString("ignore-resources=META-INF\n")

val platforms: Map<String, String> = mapOf(
    "windows_x64" to windowsFile,
    "linux_x64" to linuxFile,
    "mac_x64" to macFile,
    "mac_arm64" to macArmFile,
)

platforms.forEach { (platform, nativeFile) ->
    tasks.register<Jar>("nativeJar_${platform}") {
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
                logger.warn("Missing LibA TeaVM C static archive for $platform: $nativeFile")
            }
        }
    }
}
