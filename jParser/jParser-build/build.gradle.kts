plugins {
    id("java-library")
}

val moduleName = "${LibExt.libName}-build"

dependencies {
    implementation(project(":jParser:jParser-core"))
    implementation(project(":jParser:jParser-base"))
    implementation(project(":jParser:jParser-idl"))
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