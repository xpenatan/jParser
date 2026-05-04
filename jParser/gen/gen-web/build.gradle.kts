plugins {
    id("java-library")
}

val moduleName = "gen_web"

dependencies {
    api(project(":jParser:gen:gen-idl"))
    implementation(project(":jParser:runtime:runtime-base"))
    implementation(project(":jParser:gen:gen-core"))
    implementation(project(":jParser:api:api-core"))
}

java {
    sourceCompatibility = JavaVersion.toVersion(LibExt.javaWebTarget)
    targetCompatibility = JavaVersion.toVersion(LibExt.javaWebTarget)
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