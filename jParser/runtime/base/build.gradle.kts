plugins {
    id("java-library")
}

val moduleName = "runtime-base"

dependencies {
    implementation(project(":jParser:loader:loader-core"))
    implementation(project(":jParser:api:api-core"))
}

java {
    sourceCompatibility = JavaVersion.toVersion(libs.versions.javaMain.get())
    targetCompatibility = JavaVersion.toVersion(libs.versions.javaMain.get())
}

tasks {
    withType<Jar> {
        from(sourceSets["main"].allSource)
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }
}

java {
    withJavadocJar()
    withSourcesJar()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifactId = moduleName
            group = project.group.toString()
            version = project.version.toString()
            from(components["java"])
        }
    }
}