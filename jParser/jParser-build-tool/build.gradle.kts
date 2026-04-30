plugins {
    id("java")
}

val moduleName = "${LibExt.libName}-build-tool"

dependencies {
    implementation(project(":jParser:jParser-core"))
    implementation(project(":jParser:jParser-idl"))
    implementation(project(":jParser:jParser-teavm-web"))
    implementation(project(":jParser:jParser-jni"))
    implementation(project(":jParser:jParser-ffm"))
    implementation(project(":jParser:jParser-build"))
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