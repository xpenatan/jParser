plugins {
    id("java-library")
}

val moduleName = "loader-teavm"

dependencies {
    implementation(project(":loader:loader-core"))
    implementation("org.teavm:teavm-jso:${LibExt.teaVMVersion}")
    implementation("org.teavm:teavm-jso-apis:${LibExt.teaVMVersion}")
    implementation("org.teavm:teavm-jso-impl:${LibExt.teaVMVersion}")

    implementation("com.github.xpenatan:jMultiplatform:${LibExt.jMultiplatform}")
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