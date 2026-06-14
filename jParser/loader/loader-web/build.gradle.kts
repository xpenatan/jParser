plugins {
    id("java-library")
}

val moduleName = "loader-web"

dependencies {
    implementation(project(":jParser:loader:loader-core"))
    api("org.teavm:teavm-jso:${LibExt.teaVMVersion}")
    api("org.teavm:teavm-jso-apis:${LibExt.teaVMVersion}")
    api("org.teavm:teavm-jso-impl:${LibExt.teaVMVersion}")
    api("org.teavm:teavm-extension-spi:${LibExt.teaVMVersion}")

    implementation("com.github.xpenatan:jMultiplatform:${LibExt.jMultiplatform}")
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