plugins {
    id("java")
}

val mainClassName = "com.github.xpenatan.jparser.example.Main"

dependencies {
    implementation(project(":example:example-base"))
    implementation(project(":jParser:core"))
    implementation(project(":jParser:idl"))
    implementation(project(":jParser:teavm"))
    implementation(project(":jParser:cpp"))
}

tasks.register<JavaExec>("generateNativeProject") {
    group = "gen"
    description = "Generate native project"
    mainClass.set(mainClassName)
    classpath = sourceSets["main"].runtimeClasspath
}