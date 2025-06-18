import java.io.File
import java.util.*

object LibExt {
    const val libName = "jParser"
    val libVersion: String = getVersion()
    const val groupId = "com.github.xpenatan.jParser"

    // Lib Dependencies
    const val jniGenVersion = "2.5.1"
    const val teaVMVersion = "0.12.1"
    const val javaparserVersion = "3.26.1"

    // Example Dependencies
    const val exampleUseRepoLibs = false
    const val gdxVersion = "1.13.5"
    const val gdxTeaVMVersion = "-SNAPSHOT"

    const val jUnitVersion = "4.13.2"
}

private fun getVersion(): String {
    val isReleaseStr = System.getenv("RELEASE")
    val isRelease = isReleaseStr != null && isReleaseStr.toBoolean()
    var libVersion = "-SNAPSHOT"
    val file = File("gradle.properties")
    if(file.exists()) {
        val properties = Properties()
        properties.load(file.inputStream())
        val version = properties.getProperty("version")
        if(isRelease) {
            libVersion = version
        }
    }
    else {
        if(isRelease) {
            throw RuntimeException("properties should exist")
        }
    }
    println("Lib Version: $libVersion")
    return libVersion
}