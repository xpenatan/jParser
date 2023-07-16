plugins {
    id("java")
}

val moduleName = "jParser-idl"

dependencies {
    implementation(project(":jParser:core"))
    testImplementation("junit:junit:${LibExt.jUnitVersion}")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifactId = moduleName
            from(components["java"])
        }
    }
}