plugins {
    id("java")
}

val moduleName = "${LibExt.libName}-base"

tasks {
    withType<Jar> {
        from(sourceSets["main"].allSource)
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }
}

dependencies {
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifactId = moduleName
            from(components["java"])
        }
    }
}