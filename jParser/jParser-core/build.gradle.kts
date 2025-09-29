plugins {
    id("java-library")
}

val moduleName = "${LibExt.libName}-core"

dependencies {
    api("com.github.javaparser:javaparser-symbol-solver-core:${LibExt.javaparserVersion}")
    api("com.github.javaparser:javaparser-core:${LibExt.javaparserVersion}")
}

java {
    sourceCompatibility = JavaVersion.toVersion(LibExt.java11Target)
    targetCompatibility = JavaVersion.toVersion(LibExt.java11Target)
}

java {
    withJavadocJar()
    withSourcesJar()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifactId = moduleName
            from(components["java"])
        }
    }
}