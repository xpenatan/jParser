plugins {
    id("java-library")
}

val moduleName = "loader-teavm"

dependencies {
    implementation(project(":jParser:loader:loader-core"))
    implementation("org.teavm:teavm-jso:${LibExt.teaVMVersion}")
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