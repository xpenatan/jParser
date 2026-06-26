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

    val hostDesktopPlatform: String
        get() {
            val os = System.getProperty("os.name").lowercase(Locale.ROOT)
            val arch = System.getProperty("os.arch").lowercase(Locale.ROOT)
            return when {
                os.contains("windows") -> "windows_x64"
                os.contains("linux") && isX64(arch) -> "linux_x64"
                os.contains("mac") && isArm64(arch) -> "mac_arm64"
                os.contains("mac") && isX64(arch) -> "mac_x64"
                else -> throw IllegalStateException("Unsupported desktop host: os=$os arch=$arch")
            }
        }

    val hostJParserTargetPrefix: String
        get() = when(hostDesktopPlatform) {
            "windows_x64" -> "windows64"
            "linux_x64" -> "linux64"
            "mac_x64" -> "mac64"
            "mac_arm64" -> "macArm"
            else -> throw IllegalStateException("Unsupported desktop host: $hostDesktopPlatform")
        }

    fun buildProjectTask(buildProjectPath: String, taskPrefix: String, target: String? = null): String {
        val targetSuffix = if(target == null) "" else "_$target"
        return "$buildProjectPath:${taskPrefix}_build_project$targetSuffix"
    }

    fun hostBuildProjectTask(buildProjectPath: String, taskPrefix: String, api: String): String {
        return buildProjectTask(buildProjectPath, taskPrefix, "${hostJParserTargetPrefix}_$api")
    }

    fun hostPluginTask(pluginProjectPath: String, api: String): String {
        return "$pluginProjectPath:jParser_build_${hostJParserTargetPrefix}_$api"
    }

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

    private fun isX64(arch: String): Boolean {
        return arch == "x86_64" || arch == "amd64"
    }

    private fun isArm64(arch: String): Boolean {
        return arch == "aarch64" || arch == "arm64"
    }
}
