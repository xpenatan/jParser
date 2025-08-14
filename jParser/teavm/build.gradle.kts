plugins {
    id("java-library")
}

val moduleName = "jParser-teavm"

dependencies {
    api(project(":jParser:idl"))
    implementation(project(":jParser:core"))
}

java {
    sourceCompatibility = JavaVersion.toVersion(LibExt.java8Target)
    targetCompatibility = JavaVersion.toVersion(LibExt.java8Target)
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