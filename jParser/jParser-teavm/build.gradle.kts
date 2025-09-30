plugins {
    id("java-library")
}

val moduleName = "jParser-teavm"

dependencies {
    api(project(":jParser:jParser-idl"))
    implementation(project(":jParser:jParser-core"))
    implementation(project(":idl:idl-core"))
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
            group = LibExt.groupId
            version = LibExt.libVersion
            from(components["java"])
        }
    }
}