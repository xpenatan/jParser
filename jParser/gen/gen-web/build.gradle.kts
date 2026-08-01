plugins {
    id("java-library")
}

val moduleName = "gen-web"
fun jParserModule(name: String) = providers.provider {
    "${libs.versions.jParserGroup.get()}:$name:${project.version}"
}

dependencies {
    api(project(":gen-idl"))
    implementation(jParserModule("runtime-base"))
    implementation(project(":gen-core"))
    implementation(jParserModule("api-core"))
}

java {
    sourceCompatibility = JavaVersion.toVersion(libs.versions.javaWeb.get())
    targetCompatibility = JavaVersion.toVersion(libs.versions.javaWeb.get())
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
