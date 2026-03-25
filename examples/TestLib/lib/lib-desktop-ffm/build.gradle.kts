plugins {
    id("java")
    id("java-library")
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

dependencies {
    if(LibExt.exampleUseRepoLibs) {
        api("com.github.xpenatan.jParser:loader-core:-SNAPSHOT")
        api("com.github.xpenatan.jParser:idl-core:-SNAPSHOT")
        api("com.github.xpenatan.jParser:idl-helper-desktop-ffm:-SNAPSHOT")
    }
    else {
        api(project(":loader:loader-core"))
        api(project(":idl:idl-core"))
        api(project(":idl-helper:idl-helper-desktop-ffm"))
    }
}

// Bundle FFM-compiled native libraries into the JAR.
val libDir = "${projectDir}/../lib-build/build/c++/libs"
val windowsFile = "$libDir/windows/vc/ffm/TestLib64.dll"
val linuxFile = "$libDir/linux/ffm/libTestLib64.so"
val macFile = "$libDir/mac/ffm/libTestLib64.dylib"
val macArmFile = "$libDir/mac/arm/ffm/libTestLibarm64.dylib"

tasks.jar {
    from(windowsFile)
    from(linuxFile)
    from(macFile)
    from(macArmFile)
}

tasks.named("clean") {
    doFirst {
        val srcPath = "$projectDir/src/main/"
        project.delete(files(srcPath))
    }
}
