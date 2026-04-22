plugins {
    id("java")
}

val moduleName = "idl-helper-desktop-jni"

java {
    sourceCompatibility = JavaVersion.toVersion(LibExt.java8Target)
    targetCompatibility = JavaVersion.toVersion(LibExt.java8Target)
}

val libDir = "${projectDir}/../idl-helper-build/build/c++/libs"
val windowsFile = "$libDir/windows/vc/jni/idl64.dll"
val linuxFile = "$libDir/linux/jni/libidl64.so"
val macFile = "$libDir/mac/jni/libidl64.dylib"
val macArmFile = "$libDir/mac/arm/jni/libidlarm64.dylib"

tasks.jar {
    from(windowsFile)
    from(linuxFile)
    from(macFile)
    from(macArmFile)
}

dependencies {
    implementation(project(":idl:idl-core"))
    implementation(project(":loader:loader-core"))
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