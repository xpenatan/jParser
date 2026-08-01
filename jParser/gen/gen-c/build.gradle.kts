plugins {
    id("java-library")
}

val moduleName = "gen-c"
fun jParserModule(name: String) = providers.provider {
    "${libs.versions.jParserGroup.get()}:$name:${project.version}"
}

dependencies {
    implementation(project(":gen-idl"))
    implementation(project(":gen-core"))
    implementation(project(":gen-ffm"))
    implementation(jParserModule("api-core"))

    testImplementation(libs.junit)
}

java {
    sourceCompatibility = JavaVersion.toVersion(libs.versions.javaMain.get())
    targetCompatibility = JavaVersion.toVersion(libs.versions.javaMain.get())
}

tasks.named<JavaCompile>("compileJava") {
    // Enforce the published Java 8 API surface, not only Java 8 bytecode.
    options.release.set(8)
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
