plugins {
    id("java-library")
}

val moduleName = "jParser-teavm"

dependencies {
    api(project(":jParser:jParser-idl"))
    implementation(project(":jParser:jParser-core"))
    implementation(project(":idl:api:api-core"))
}

java {
    sourceCompatibility = JavaVersion.toVersion(LibExt.javaWebTarget)
    targetCompatibility = JavaVersion.toVersion(LibExt.javaWebTarget)
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