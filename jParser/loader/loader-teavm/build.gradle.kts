plugins {
    id("java-library")
}

val moduleName = "loader-teavm"

dependencies {
    api("com.badlogicgames.gdx:gdx-jnigen-loader:${LibExt.jniGenVersion}")
    implementation("org.teavm:teavm-classlib:${LibExt.teaVMVersion}")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifactId = moduleName
            from(components["java"])
        }
    }
}