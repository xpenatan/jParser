plugins {
    id("java-library")
}

val moduleName = "loader-teavm"

dependencies {
    implementation(project(":jParser:loader:loader-core"))
    implementation("org.teavm:teavm-jso:${LibExt.teaVMVersion}")
    implementation("org.teavm:teavm-jso-apis:${LibExt.teaVMVersion}")
    implementation("org.teavm:teavm-jso-impl:${LibExt.teaVMVersion}")

    implementation("com.github.xpenatan:jMultiplatform:0.1.2")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifactId = moduleName
            from(components["java"])
        }
    }
}