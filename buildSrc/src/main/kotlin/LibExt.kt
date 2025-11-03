import java.io.File
import java.util.*

object LibExt {
    const val libName = "jParser"
    const val groupId = "com.github.xpenatan.jParser"
    var isRelease = false
    var libVersion: String = ""
        get() {
            return getVersion()
        }

    const val java8Target = "1.8"
    const val java11Target = "11"

    // Lib Dependencies
    const val jniGenVersion = "2.5.1"
    const val teaVMVersion = "0.13.0"
    const val javaparserVersion = "3.26.1"
    const val jMultiplatform = "0.1.3"

    // Example Dependencies
    const val exampleUseRepoLibs = false
    const val gdxVersion = "1.14.0"
    const val gdxTeaVMVersion = "-SNAPSHOT"

    const val jUnitVersion = "4.13.2"
}

private fun getVersion(): String {
    var libVersion = "-SNAPSHOT"
    val file = File("gradle.properties")
    if(file.exists()) {
        val properties = Properties()
        properties.load(file.inputStream())
        val version = properties.getProperty("version")
        if(LibExt.isRelease) {
            libVersion = version
        }
    }
    else {
        if(LibExt.isRelease) {
            throw RuntimeException("properties should exist")
        }
    }
    return libVersion
}