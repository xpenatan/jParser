plugins {
    id("java")
}

val mainClassName = "com.github.xpenatan.jparser.example.Main"

dependencies {
    implementation(project(":example:lib:base"))
    if(LibExt.exampleUseRepoLibs) {
        implementation("com.github.xpenatan.jParser:jParser-core:${LibExt.libVersion}")
        implementation("com.github.xpenatan.jParser:jParser-idl:${LibExt.libVersion}")
        implementation("com.github.xpenatan.jParser:jParser-teavm:${LibExt.libVersion}")
        implementation("com.github.xpenatan.jParser:jParser-cpp:${LibExt.libVersion}")
    }
    else {
        implementation(project(":jParser:core"))
        implementation(project(":jParser:idl"))
        implementation(project(":jParser:teavm"))
        implementation(project(":jParser:cpp"))
    }
}

tasks.register<JavaExec>("generateNativeProject") {
    group = "gen"
    description = "Generate native project"
    mainClass.set(mainClassName)
    classpath = sourceSets["main"].runtimeClasspath
}