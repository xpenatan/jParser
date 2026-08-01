plugins {
    id("java-library")
}

val moduleName = "gen-jni"
fun jParserModule(name: String) = providers.provider {
    "${libs.versions.jParserGroup.get()}:$name:${project.version}"
}

dependencies {
    implementation(project(":gen-idl"))
    implementation(project(":gen-core"))
    implementation(jParserModule("api-core"))

    testImplementation(jParserModule("loader-core"))
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
