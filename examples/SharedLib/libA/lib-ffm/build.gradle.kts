plugins {
    id("java")
    id("java-library")
}

// FFM (java.lang.foreign.*) requires JDK 22+ at runtime.
// We compile with JDK 25 to access the FFM API, but set targetCompatibility
// to Java 11 so Gradle's dependency metadata stays compatible with lower-JVM
// consumer modules.  The --release flag is cleared so the JDK 25 API is
// available despite the Java 11 bytecode target.
// It is the consumer's responsibility to run the application on JDK 22+.
java {
    sourceCompatibility = JavaVersion.toVersion(LibExt.java8Target)
    targetCompatibility = JavaVersion.toVersion(LibExt.java8Target)
}

tasks.withType<JavaCompile> {
    javaCompiler.set(javaToolchains.compilerFor {
        languageVersion.set(JavaLanguageVersion.of(25))
    })
    // Clear --release so JDK 25 APIs (java.lang.foreign) are accessible
    // even with -source 11 -target 11 bytecode output.
    options.release.set(null as Int?)
}

dependencies {
    if(LibExt.exampleUseRepoLibs) {
        api("com.github.xpenatan.jParser:loader-core:-SNAPSHOT")
        api("com.github.xpenatan.jParser:idl-core:-SNAPSHOT")
        api("com.github.xpenatan.jParser:idl-helper-ffm:-SNAPSHOT")
    }
    else {
        api(project(":loader:loader-core"))
        api(project(":idl:idl-core"))
        api(project(":idl-helper:idl-helper-ffm"))
    }
}

tasks.named("clean") {
    doFirst {
        val srcPath = "$projectDir/src/main/"
        project.delete(files(srcPath))
    }
}

