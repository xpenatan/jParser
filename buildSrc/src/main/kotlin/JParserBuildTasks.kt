import java.util.Locale

object JParserBuildTasks {
    fun hostBuildProjectTask(buildProjectPath: String, taskPrefix: String, api: String): String {
        return "$buildProjectPath:${taskPrefix}_build_project_${hostTargetPrefix}_$api"
    }

    private val hostTargetPrefix: String
        get() {
            val os = System.getProperty("os.name").lowercase(Locale.ROOT)
            val arch = System.getProperty("os.arch").lowercase(Locale.ROOT)
            return when {
                os.contains("windows") -> "windows64"
                os.contains("linux") && isX64(arch) -> "linux64"
                os.contains("mac") && isArm64(arch) -> "macArm"
                os.contains("mac") && isX64(arch) -> "mac64"
                else -> throw IllegalStateException("Unsupported desktop host: os=$os arch=$arch")
            }
        }

    private fun isX64(arch: String): Boolean {
        return arch == "x86_64" || arch == "amd64"
    }

    private fun isArm64(arch: String): Boolean {
        return arch == "aarch64" || arch == "arm64"
    }
}
