plugins {
    id("java")
}

val moduleName = "${LibExt.libName}-build-tool"

dependencies {
    implementation(project(":jParser:core"))
    implementation(project(":jParser:idl"))
    implementation(project(":jParser:teavm"))
    implementation(project(":jParser:cpp"))
    implementation(project(":jParser:builder"))
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