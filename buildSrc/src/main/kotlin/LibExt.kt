import java.io.File
import java.util.*

object LibExt {
    const val libName = "jParser"
    const val groupId = "com.github.xpenatan.jParser"
    private var configuredRootDirectory: File? = null
    var isRelease = false
    var libVersion: String = ""
        get() = getVersion()

    val rootDirectory: File
        get() {
            val configured = configuredRootDirectory
            if(configured != null) {
                return configured
            }
            val discovered = findRootDirectory(File(System.getProperty("user.dir")))
            configuredRootDirectory = discovered
            return discovered
        }

    fun configure(startDirectory: File) {
        configuredRootDirectory = findRootDirectory(startDirectory)
    }

    const val javaMainTarget = "1.8"
    const val javaWebTarget = "17"
    const val javaFFMTarget = "25"

    // Lib Dependencies
    const val jniGenVersion = "2.5.1"
    const val teaVMVersion = "0.15.0"
    const val javaparserVersion = "3.26.1"
    const val jMultiplatform = "0.1.3"

    // Example Dependencies
    const val gdxVersion = "1.14.0"
    const val gdxTeaVMVersion = "1.5.6"

    const val jUnitVersion = "4.13.2"

    private fun getVersion(): String {
        var libVersion = "-SNAPSHOT"
        val file = File(rootDirectory, "gradle.properties")
        if(file.exists()) {
            val properties = Properties()
            properties.load(file.inputStream())
            val version = properties.getProperty("version")
            if(isRelease) {
                libVersion = version
            }
        }
        else if(isRelease) {
            throw RuntimeException("properties should exist")
        }
        return libVersion
    }

    private fun findRootDirectory(startDirectory: File): File {
        var directory: File? = if(startDirectory.isFile) startDirectory.parentFile else startDirectory
        while(directory != null) {
            val candidate = File(directory, "gradle.properties")
            if(candidate.isFile) {
                return directory
            }
            directory = directory.parentFile
        }
        if(isRelease) {
            throw RuntimeException("properties should exist")
        }
        return File(System.getProperty("user.dir"))
    }
}
