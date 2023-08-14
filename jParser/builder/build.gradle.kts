plugins {
    id("java-library")
}

val moduleName = "jParser-compiler"

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