plugins {
    id("java")
}

val moduleName = "gen-build-tool"

dependencies {
    implementation(project(":jParser:gen:gen-core"))
    implementation(project(":jParser:gen:gen-idl"))
    implementation(project(":jParser:gen:gen-web"))
    implementation(project(":jParser:gen:gen-c"))
    implementation(project(":jParser:gen:gen-jni"))
    implementation(project(":jParser:gen:gen-ffm"))
    implementation(project(":jParser:gen:gen-build"))

    testImplementation(libs.junit)
}

java {
    sourceCompatibility = JavaVersion.toVersion(libs.versions.javaFfm.get())
    targetCompatibility = JavaVersion.toVersion(libs.versions.javaFfm.get())
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
