plugins {
    id("java-library")
}

val moduleName = "loader-web"

dependencies {
    implementation(project(":jParser:loader:loader-core"))
    api(libs.teavmJso)
    api(libs.teavmJsoApis)
    api(libs.teavmJsoImpl)

    implementation(libs.jMultiplatform)
}

java {
    sourceCompatibility = JavaVersion.toVersion(libs.versions.javaWeb.get())
    targetCompatibility = JavaVersion.toVersion(libs.versions.javaWeb.get())
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
