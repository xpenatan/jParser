plugins {
    id("java-library")
}

val moduleName = "gen-build"
fun jParserModule(name: String) = providers.provider {
    "${libs.versions.jParserGroup.get()}:$name:${project.version}"
}

dependencies {
    api(jParserModule("api-core"))

    implementation(project(":gen-core"))
    implementation(project(":gen-idl"))
    implementation(project(":gen-jni"))
    implementation(project(":gen-ffm"))

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
