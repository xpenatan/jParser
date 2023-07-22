plugins {
    id("java-library")
}

val moduleName = "jParser-idl"

sourceSets["main"].resources.setSrcDirs(arrayListOf(
    "../base/src/main/java/",
    "../base/src/main/resources/"
))

dependencies {
    implementation(project(":jParser:base"))
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