plugins {
    id("java-library")
}

val moduleName = "gen-ffm"

dependencies {
    implementation(project(":jParser:gen:gen-idl"))
    implementation(project(":jParser:gen:gen-core"))
    implementation(project(":jParser:api:api-core"))

    testImplementation(project(":jParser:loader:loader-core"))
    testImplementation("junit:junit:${LibExt.jUnitVersion}")
}

java {
    sourceCompatibility = JavaVersion.toVersion(LibExt.javaFFMTarget)
    targetCompatibility = JavaVersion.toVersion(LibExt.javaFFMTarget)
}

java {
    withJavadocJar()
    withSourcesJar()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifactId = moduleName
            group = LibExt.groupId
            version = LibExt.libVersion
            from(components["java"])
        }
    }
}


