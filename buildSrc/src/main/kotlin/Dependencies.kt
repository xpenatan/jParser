object LibExt {
    val libVersion: String = this.getVersion()
    const val jniGenVersion = "2.3.1"
    const val javaparserVersion = "3.24.7"
    const val jUnitVersion = "4.12"
    const val groupId = "com.github.xpenatan.jParser"
    const val exampleUseRepoLibs = false
    const val gdxVersion = "1.12.1"
    const val teaVMVersion = "0.9.0"

    private fun getVersion(): String {
        var isRelease = System.getenv("RELEASE")
        var libVersion = "1.0.0-SNAPSHOT"
        if(isRelease != null && isRelease.toBoolean()) {
            libVersion = "1.0.0-b4"
        }
        System.out.println("jParser Version: " + libVersion)
        return libVersion
    }
}