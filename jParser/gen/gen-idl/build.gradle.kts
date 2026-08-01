plugins {
    id("java-library")
}

val moduleName = "gen-idl"
fun jParserModule(name: String) = providers.provider {
    "${libs.versions.jParserGroup.get()}:$name:${project.version}"
}

dependencies {
    implementation(jParserModule("runtime-base"))
    implementation(project(":gen-core"))
    implementation(jParserModule("api-core"))
    testImplementation(libs.junit)
}

java {
    sourceCompatibility = JavaVersion.toVersion(libs.versions.javaMain.get())
    targetCompatibility = JavaVersion.toVersion(libs.versions.javaMain.get())
}

java {
    withJavadocJar()
    withSourcesJar()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifactId = moduleName
            from(components["java"])
        }
    }
}
