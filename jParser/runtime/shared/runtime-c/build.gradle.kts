plugins {
    id("java-library")
}

val moduleName = "runtime-c"

dependencies {
    api(project(":jParser:runtime:core"))

    implementation(project(":jParser:api:api-core"))
    api(project(":jParser:loader:loader-c"))

    api(libs.bundles.teavmC)
}

tasks.named("compileJava") {
    dependsOn(":jParser:runtime:builder:runtime_helper_build_project")
}

tasks.named("processResources") {
    dependsOn(":jParser:runtime:builder:runtime_helper_build_project")
}

tasks.named("clean") {
    doFirst {
        val srcPath = "$projectDir/src/main/java"
        project.delete(files(srcPath))
    }
}

java {
    sourceCompatibility = JavaVersion.toVersion(libs.versions.javaWeb.get())
    targetCompatibility = JavaVersion.toVersion(libs.versions.javaWeb.get())
}

sourceSets {
    main {
        java.setSrcDirs(listOf("src/main/java", "src/main/support/java"))
        resources.setSrcDirs(listOf("src/main/resources", "build/generated/jparser/resources/main"))
    }
}

java {
    withJavadocJar()
    withSourcesJar()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifactId = moduleName
            groupId = project.group.toString()
            version = project.version.toString()
            from(components["java"])
        }
    }
}
