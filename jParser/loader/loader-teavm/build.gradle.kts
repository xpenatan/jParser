plugins {
    id("java-library")
}

val moduleName = "loader-teavm"

dependencies {
    api("com.badlogicgames.gdx:gdx-jnigen-loader:${LibExt.jniGenVersion}")
    implementation("com.github.xpenatan.gdx-teavm:asset-loader:${LibExt.gdxTeaVMVersion}")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifactId = moduleName
            from(components["java"])
        }
    }
}