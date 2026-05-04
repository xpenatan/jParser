plugins {
    id("java-library")
}

val moduleName = "gen_build"

dependencies {
    implementation(project(":jParser:gen:gen-core"))
    implementation(project(":jParser:gen:gen-idl"))
    implementation(project(":jParser:gen:gen-ffm"))
}

java {
    sourceCompatibility = JavaVersion.toVersion(LibExt.javaMainTarget)
    targetCompatibility = JavaVersion.toVersion(LibExt.javaMainTarget)
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