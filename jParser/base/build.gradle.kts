plugins {
    id("java")
}

val moduleName = "jParser-base"

dependencies {
}

val sourcesJar by tasks.creating(Jar::class) {
    from(sourceSets.main.get().allSource)
}

tasks {
    artifacts {
        archives(sourcesJar)
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifactId = moduleName
            artifact(sourcesJar)
        }
    }
}