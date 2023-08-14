plugins {
    id("java-library")
}

val moduleName = "cpp"

dependencies {
    implementation(project(":jParser:idl"))
    implementation(project(":jParser:core"))
    implementation("com.badlogicgames.gdx:gdx-jnigen:${LibExt.jniGenVersion}")

    testImplementation(project(":jParser:loader:loader-core"))
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