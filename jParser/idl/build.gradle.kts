plugins {
    id("java-library")
}

val moduleName = "idl"

dependencies {
    implementation(project(":jParser:base"))
    implementation(project(":jParser:core"))
    implementation("org.reflections:reflections:${LibExt.reflectionVersion}")
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