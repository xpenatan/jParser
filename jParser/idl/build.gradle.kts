plugins {
    id("java-library")
}

val moduleName = "${LibExt.libName}-idl"

dependencies {
    implementation(project(":jParser:base"))
    implementation(project(":jParser:core"))
    testImplementation("junit:junit:${LibExt.jUnitVersion}")
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