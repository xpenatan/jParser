plugins {
    id("java")
}

val moduleName = "idl-helper-ffm"

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

