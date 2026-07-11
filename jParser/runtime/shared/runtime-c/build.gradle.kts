plugins {
    id("java-library")
}

val moduleName = "runtime-c"

dependencies {
    api(project(":jParser:runtime:core"))

    implementation(project(":jParser:api:api-core"))
    api(project(":jParser:loader:loader-c"))

    api("org.teavm:teavm-core:${LibExt.teaVMVersion}")
    api("org.teavm:teavm-classlib:${LibExt.teaVMVersion}")
    api("org.teavm:teavm-extension-spi:${LibExt.teaVMVersion}")
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
    sourceCompatibility = JavaVersion.toVersion(LibExt.javaWebTarget)
    targetCompatibility = JavaVersion.toVersion(LibExt.javaWebTarget)
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
            groupId = LibExt.groupId
            version = LibExt.libVersion
            from(components["java"])
        }
    }
}
