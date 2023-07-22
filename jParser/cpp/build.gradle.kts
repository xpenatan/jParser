plugins {
    id("java-library")
}

val moduleName = "jParser-cpp"

dependencies {
    implementation(project(":jParser:idl"))
    implementation(project(":jParser:core"))
    implementation("com.badlogicgames.gdx:gdx-jnigen:${LibExt.jniGenVersion}")

    testImplementation(project(":jParser:loader"))
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