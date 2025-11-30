plugins {
    id("java-library")
}

val moduleName = "idl-helper-base"

dependencies {
    implementation(project(":loader:loader-core"))
    implementation(project(":idl:idl-core"))
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