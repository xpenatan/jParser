plugins {
    id("java")
}

val moduleName = "runtime-jni"

dependencies {
    implementation(project(":jParser:api:api-core"))
    implementation(project(":jParser:loader:loader-core"))
}

tasks.named("compileJava") {
    dependsOn(":jParser:runtime:builder:runtime_helper_build_project")
}

tasks.named("clean") {
    doFirst {
        val srcPath = "$projectDir/src/main/"
        project.delete(files(srcPath))
    }
}

java {
    sourceCompatibility = JavaVersion.toVersion(LibExt.javaMainTarget)
    targetCompatibility = JavaVersion.toVersion(LibExt.javaMainTarget)
}

java {
    withJavadocJar()
    withSourcesJar()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifactId = moduleName
            groupId = LibExt.groupId
            version = LibExt.libVersion
            from(components["java"])
        }
    }
}
