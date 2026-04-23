plugins {
    id("java")
}

val moduleName = "idl-helper-desktop-ffm"

dependencies {
    implementation(project(":idl:idl-core"))
    implementation(project(":loader:loader-core"))
}

java {
    sourceCompatibility = JavaVersion.toVersion(LibExt.javaFFMTarget)
    targetCompatibility = JavaVersion.toVersion(LibExt.javaFFMTarget)
}

// Bundle FFM-compiled native libraries into the JAR.
val libDir = "${projectDir}/../idl-helper-build/build/c++/libs"
val windowsFile = "$libDir/windows/vc/ffm/idl64.dll"
val linuxFile = "$libDir/linux/ffm/libidl64.so"
val macFile = "$libDir/mac/ffm/libidl64.dylib"
val macArmFile = "$libDir/mac/arm/ffm/libidlarm64.dylib"

tasks.jar {
    from(windowsFile)
    from(linuxFile)
    from(macFile)
    from(macArmFile)
}

java {
    withJavadocJar()
    withSourcesJar()
}

tasks.named("clean") {
    doFirst {
        val srcPath = "$projectDir/src/main/java"
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
