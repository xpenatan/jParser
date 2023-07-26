plugins {
    id("java-library")
}

val moduleName = "loader-teavm"

dependencies {
    api("com.badlogicgames.gdx:gdx-jnigen-loader:${LibExt.jniGenVersion}")
    implementation("org.teavm:teavm-core:${LibExt.teaVMVersion}")
//    implementation("org.teavm:teavm-jso-apis:${LibExt.teaVMVersion}")
//    implementation("org.teavm:teavm-classlib:${LibExt.teaVMVersion}")
//    implementation("org.teavm:teavm-junit:${LibExt.teaVMVersion}")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifactId = moduleName
            from(components["java"])
        }
    }
}