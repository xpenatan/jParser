plugins {
    id("java-library")
}

val moduleName = "gen-c"

dependencies {
    implementation(project(":jParser:gen:gen-idl"))
    implementation(project(":jParser:gen:gen-core"))
    implementation(project(":jParser:gen:gen-ffm"))
    implementation(project(":jParser:api:api-core"))

    testImplementation(libs.junit)
}

java {
    sourceCompatibility = JavaVersion.toVersion(libs.versions.javaMain.get())
    targetCompatibility = JavaVersion.toVersion(libs.versions.javaMain.get())
}

tasks.named<JavaCompile>("compileJava") {
    // Enforce the published Java 8 API surface, not only Java 8 bytecode.
    options.release.set(8)
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
