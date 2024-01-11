plugins {
    id("java-library")
}

val moduleName = "loader-core"

dependencies {
    api("com.badlogicgames.gdx:gdx-jnigen-loader:${LibExt.jniGenVersion}")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifactId = moduleName
            from(components["java"])
        }
    }
}