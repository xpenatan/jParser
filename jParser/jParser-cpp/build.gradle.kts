plugins {
    id("java-library")
}

val moduleName = "${LibExt.libName}-cpp"

dependencies {
    implementation(project(":jParser:jParser-idl"))
    implementation(project(":jParser:jParser-core"))

    testImplementation(project(":loader:loader-core"))
    testImplementation("junit:junit:${LibExt.jUnitVersion}")
}

java {
    sourceCompatibility = JavaVersion.toVersion(LibExt.java8Target)
    targetCompatibility = JavaVersion.toVersion(LibExt.java8Target)
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