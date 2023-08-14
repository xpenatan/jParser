plugins {
    id("java-library")
}

val moduleName = "compiler"

dependencies {
    implementation(project(":jParser:core"))
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifactId = moduleName
            from(components["java"])
        }
    }
}