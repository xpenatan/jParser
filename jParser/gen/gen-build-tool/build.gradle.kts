plugins {
    id("java")
}

val moduleName = "gen-build-tool"

dependencies {
    implementation(project(":gen-core"))
    implementation(project(":gen-idl"))
    implementation(project(":gen-web"))
    implementation(project(":gen-c"))
    implementation(project(":gen-jni"))
    implementation(project(":gen-ffm"))
    implementation(project(":gen-build"))

    testImplementation(libs.junit)
}

java {
    sourceCompatibility = JavaVersion.toVersion(libs.versions.javaFfm.get())
    targetCompatibility = JavaVersion.toVersion(libs.versions.javaFfm.get())
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
