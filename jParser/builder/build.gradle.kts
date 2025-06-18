plugins {
    id("java-library")
}

val moduleName = "${LibExt.libName}-build"

dependencies {
    implementation(project(":jParser:core"))
    implementation(project(":jParser:base"))
    implementation(project(":jParser:idl"))
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifactId = moduleName
            from(components["java"])
        }
    }
}