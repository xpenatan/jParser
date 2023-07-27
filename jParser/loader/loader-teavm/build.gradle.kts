plugins {
    id("java-library")
}

val moduleName = "loader-teavm"

dependencies {
    api("com.badlogicgames.gdx:gdx-jnigen-loader:${LibExt.jniGenVersion}")
    implementation("org.teavm:teavm-core:${LibExt.teaVMVersion}")
    implementation("com.github.xpenatan.gdx-teavm:backend-teavm:1.0.0-SNAPSHOT")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifactId = moduleName
            from(components["java"])
        }
    }
}