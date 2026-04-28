plugins {
    id("java-library")
}

val moduleName = "${LibExt.libName}-jni"

dependencies {
    implementation(project(":jParser:jParser-idl"))
    implementation(project(":jParser:jParser-core"))
    implementation(project(":idl:api:api-core"))

    testImplementation(project(":loader:loader-core"))
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