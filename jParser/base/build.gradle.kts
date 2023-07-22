plugins {
    id("java")
}

val moduleName = "jParser-base"

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