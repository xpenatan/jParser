plugins {
    id("java-library")
}

val moduleName = "gen-web"

dependencies {
    api(project(":jParser:gen:gen-idl"))
    implementation(project(":jParser:runtime:base"))
    implementation(project(":jParser:gen:gen-core"))
    implementation(project(":jParser:api:api-core"))
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
            group = project.group.toString()
            version = project.version.toString()
            from(components["java"])
        }
    }
}
