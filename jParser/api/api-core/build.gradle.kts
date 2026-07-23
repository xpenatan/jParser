plugins {
    id("java")
}

val moduleName = "api-core"

dependencies {
}

java {
    sourceCompatibility = JavaVersion.toVersion(libs.versions.javaMain.get())
    targetCompatibility = JavaVersion.toVersion(libs.versions.javaMain.get())
}

java {
    withJavadocJar()
    withSourcesJar()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifactId = moduleName
            group = project.group.toString()
            version = project.version.toString()
            from(components["java"])
        }
    }
}