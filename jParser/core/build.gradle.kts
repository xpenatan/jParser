plugins {
    id("java-library")
}

val moduleName = "jParser-core"

dependencies {
    api("com.github.javaparser:javaparser-symbol-solver-core:${LibExt.javaparserVersion}")
    api("com.github.javaparser:javaparser-core:${LibExt.javaparserVersion}")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifactId = moduleName
            from(components["java"])
        }
    }
}