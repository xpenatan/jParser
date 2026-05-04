plugins {
    id("java")
}

val moduleName = "gen_build_tool"

dependencies {
    implementation(project(":jParser:gen:gen-core"))
    implementation(project(":jParser:gen:gen-idl"))
    implementation(project(":jParser:gen:gen-web"))
    implementation(project(":jParser:gen:gen-jni"))
    implementation(project(":jParser:gen:gen-ffm"))
    implementation(project(":jParser:gen:gen-build"))
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