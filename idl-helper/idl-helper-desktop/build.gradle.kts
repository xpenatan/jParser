plugins {
    id("java")
}

val moduleName = "idl-helper-desktop"

java {
    sourceCompatibility = JavaVersion.toVersion(LibExt.java8Target)
    targetCompatibility = JavaVersion.toVersion(LibExt.java8Target)
}

val libDir = "${projectDir}/../idl-helper-build/build/c++/libs"
val windowsFile = "$libDir/windows/vc/idl64.dll"
val linuxFile = "$libDir/linux/libidl64.so"
val macFile = "$libDir/mac/libidl64.dylib"
val macArmFile = "$libDir/mac/arm/libidlarm64.dylib"

tasks.jar {
    from(windowsFile)
    from(linuxFile)
    from(macFile)
    from(macArmFile)
}

dependencies {
}

tasks.named("clean") {
    doFirst {
        val srcPath = "$projectDir/src/main/"
        project.delete(files(srcPath))
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifactId = moduleName
            group = LibExt.groupId
            version = LibExt.libVersion
            from(components["java"])
        }
    }
}