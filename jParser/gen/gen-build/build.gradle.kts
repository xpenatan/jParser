plugins {
    id("java-library")
}

val moduleName = "gen-build"

dependencies {
    api(project(":jParser:api:api-core"))

    implementation(project(":jParser:gen:gen-core"))
    implementation(project(":jParser:gen:gen-idl"))
    implementation(project(":jParser:gen:gen-jni"))
    implementation(project(":jParser:gen:gen-ffm"))

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
