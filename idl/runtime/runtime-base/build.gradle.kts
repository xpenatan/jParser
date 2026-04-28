plugins {
    id("java-library")
}

val moduleName = "runtime-base"

dependencies {
    implementation(project(":loader:loader-core"))
    implementation(project(":idl:api:api-core"))
}

java {
    sourceCompatibility = JavaVersion.toVersion(LibExt.javaMainTarget)
    targetCompatibility = JavaVersion.toVersion(LibExt.javaMainTarget)
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

//publishing {
//    publications {
//        create<MavenPublication>("maven") {
//            artifactId = moduleName
//            group = LibExt.groupId
//            version = LibExt.libVersion
//            from(components["java"])
//        }
//    }
//}