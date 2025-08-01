plugins {
    id("java-library")
}

val moduleName = "loader-core"

dependencies {
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifactId = moduleName
            from(components["java"])
        }
    }
}