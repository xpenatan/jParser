plugins {
    id("java-library")
}

val moduleName = "gen-idl"

dependencies {
    implementation(project(":jParser:runtime:base"))
    implementation(project(":jParser:gen:gen-core"))
    implementation(project(":jParser:api:api-core"))
    testImplementation(libs.junit)
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