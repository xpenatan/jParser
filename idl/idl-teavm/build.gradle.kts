plugins {
    id("java-library")
}

val moduleName = "idl-teavm"

val emscriptenJS = "$projectDir/../jolt-build/build/c++/libs/emscripten/idl.js"
val emscriptenWASM = "$projectDir/../jolt-build/build/c++/libs/emscripten/idl.wasm"

tasks.jar {
    from(emscriptenJS, emscriptenWASM)
}

dependencies {
    implementation(project(":idl:idl-core"))
    api("org.teavm:teavm-jso:${LibExt.teaVMVersion}")
    api("org.teavm:teavm-classlib:${LibExt.teaVMVersion}")
    api("org.teavm:teavm-jso:${LibExt.teaVMVersion}")
    api("org.teavm:teavm-jso-apis:${LibExt.teaVMVersion}")
    api("org.teavm:teavm-jso-impl:${LibExt.teaVMVersion}")
}

java {
    sourceCompatibility = JavaVersion.toVersion(LibExt.java11Target)
    targetCompatibility = JavaVersion.toVersion(LibExt.java11Target)
}

java {
    withJavadocJar()
    withSourcesJar()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifactId = moduleName
            group = LibExt.groupId
            version = LibExt.libVersion
            from(components["java"])
        }
    }
}