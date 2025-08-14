plugins {
    id("java")
}

val moduleName = "${LibExt.libName}-base"

dependencies {
}

java {
    sourceCompatibility = JavaVersion.toVersion(LibExt.java8Target)
    targetCompatibility = JavaVersion.toVersion(LibExt.java8Target)
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
            from(components["java"])
        }
    }
}