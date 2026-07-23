plugins {
    id("java-library")
}

val moduleName = "api-web"

val emscriptenJS = "$projectDir/../jolt-build/build/c++/libs/emscripten/idl.js"
val emscriptenWASM = "$projectDir/../jolt-build/build/c++/libs/emscripten/idl.wasm"

tasks.jar {
    from(emscriptenJS, emscriptenWASM)
}

dependencies {
    implementation(project(":jParser:api:api-core"))
    api(libs.bundles.teavmWeb)
}

java {
    sourceCompatibility = JavaVersion.toVersion(libs.versions.javaWeb.get())
    targetCompatibility = JavaVersion.toVersion(libs.versions.javaWeb.get())
}

java {
    withJavadocJar()
    withSourcesJar()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifactId = moduleName
            group = project.group.toString()
            version = project.version.toString()
            from(components["java"])
        }
    }
}
