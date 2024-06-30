import java.io.File
import java.util.*

object LibExt {
    val libVersion: String = getVersion()
    const val jniGenVersion = "2.3.1"
    const val javaparserVersion = "3.24.7"
    const val jUnitVersion = "4.13.2"
    const val groupId = "com.github.xpenatan.jParser"
    const val exampleUseRepoLibs = false
    const val gdxVersion = "1.12.1"
    const val teaVMVersion = "0.10.0"
    const val gdxTeaVMVersion = "-SNAPSHOT"

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