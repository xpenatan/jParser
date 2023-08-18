plugins {
    id("java-library")
}

val moduleName = "compiler"

dependencies {
    implementation(project(":jParser:core"))
    implementation(project(":jParser:base"))
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifactId = moduleName
            from(components["java"])
        }
    }
}