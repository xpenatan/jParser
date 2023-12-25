plugins {
    id("java")
}

val mainClassName = "Main"

dependencies {
    implementation(project(":example:lib:core"))
    implementation(project(":jParser:core"))
    implementation(project(":jParser:idl"))
    implementation(project(":jParser:teavm"))
    implementation(project(":jParser:cpp"))
    implementation(project(":jParser:builder"))
}

tasks.register<JavaExec>("build_project") {
    group = "gen"
    description = "Generate native project"
    mainClass.set(mainClassName)
    classpath = sourceSets["main"].runtimeClasspath
}