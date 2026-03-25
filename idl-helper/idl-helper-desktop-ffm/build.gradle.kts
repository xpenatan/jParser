plugins {
    id("java")
}

val moduleName = "idl-helper-desktop-ffm"

dependencies {
    implementation(project(":idl:idl-core"))
    implementation(project(":loader:loader-core"))
}

// FFM (java.lang.foreign.*) requires JDK 22+ at runtime.
// Compile with JDK 25 for API access, but target Java 8 bytecode
// so Gradle metadata stays compatible with lower-JVM consumers.
java {
    sourceCompatibility = JavaVersion.toVersion(LibExt.java8Target)
    targetCompatibility = JavaVersion.toVersion(LibExt.java8Target)
}

tasks.withType<JavaCompile> {
    javaCompiler.set(javaToolchains.compilerFor {
        languageVersion.set(JavaLanguageVersion.of(25))
    })
    options.release.set(null as Int?)
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
