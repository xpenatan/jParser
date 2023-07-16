plugins {
    id("java-library")
}

val moduleName = "jParser-teavm"

dependencies {
    api(project(":jParser:idl"))
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