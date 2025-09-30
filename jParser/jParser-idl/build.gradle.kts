plugins {
    id("java-library")
}

val moduleName = "${LibExt.libName}-idl"

dependencies {
    implementation(project(":jParser:jParser-base"))
    implementation(project(":jParser:jParser-core"))
    implementation(project(":idl:idl-core"))
    testImplementation("junit:junit:${LibExt.jUnitVersion}")
}

java {
    sourceCompatibility = JavaVersion.toVersion(LibExt.java11Target)
    targetCompatibility = JavaVersion.toVersion(LibExt.java11Target)
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