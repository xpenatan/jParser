plugins {
    id("java-library")
}

val moduleName = "gen_jni"

dependencies {
    implementation(project(":jParser:gen:gen-idl"))
    implementation(project(":jParser:gen:gen-core"))
    implementation(project(":jParser:api:api-core"))

    testImplementation(project(":jParser:loader:loader-core"))
    testImplementation("junit:junit:${LibExt.jUnitVersion}")
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