plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
    id("com.github.xpenatan.easy-publishing") version "0.1.0"
}

val moduleName = "jparser-gradle-plugin"

LibExt.configure(rootProject.projectDir)
LibExt.isRelease = rootProject.extra["easyPublishing.releaseRequested"] as Boolean

subprojects {
    group = LibExt.groupId
    version = LibExt.libVersion
    apply(plugin = "maven-publish")

    val isolatedPath = path.removePrefix(":").replace(':', '/')
    layout.buildDirectory.set(rootProject.layout.buildDirectory.dir("local-projects/$isolatedPath"))

    // The root build owns generator test execution. These mirrored projects
    // exist only to provide the local plugin compile/runtime classpath.
    tasks.withType<Test>().configureEach {
        enabled = false
    }
}

base {
    archivesName.set(moduleName)
}

dependencies {
    implementation(project(":jParser:gen:gen-build"))
    implementation(project(":jParser:gen:gen-idl"))
    implementation(project(":jParser:gen:gen-build-tool"))

    testImplementation(gradleTestKit())
    testImplementation(kotlin("test"))
    testImplementation("junit:junit:${LibExt.jUnitVersion}")
}

java {
    withJavadocJar()
    withSourcesJar()
}

gradlePlugin {
    plugins {
        create("jParser") {
            id = "com.github.xpenatan.jparser"
            implementationClass = "com.github.xpenatan.jParser.gradle.JParserGradlePlugin"
        }
    }
}

publishing {
    publications.withType<MavenPublication>().configureEach {
        if(name == "pluginMaven") {
            artifactId = moduleName
        }
    }
}

easyPublishing {
    groupId.set(LibExt.groupId)
    releaseVersion.set(LibExt.releaseVersion)
    snapshotVersion.set(LibExt.snapshotVersion)

    snapshotRepositoryUrl.set("https://central.sonatype.com/repository/maven-snapshots/")
    releaseRepositoryUrl.set("https://central.sonatype.com")
    username.set(providers.environmentVariable("CENTRAL_PORTAL_USERNAME"))
    password.set(providers.environmentVariable("CENTRAL_PORTAL_PASSWORD"))
    signingKey.set(providers.environmentVariable("SIGNING_KEY"))
    signingPassword.set(providers.environmentVariable("SIGNING_PASSWORD"))

    pomName.set("jParser Gradle plugin")
    pomDescription.set("Gradle plugin for jParser generation and native build tasks")
    projectUrl.set("https://github.com/xpenatan/jParser")

    developerId.set("Xpe")
    developerName.set("Natan")

    scmUrl.set("https://github.com/xpenatan/jParser")
    scmConnection.set("scm:git:https://github.com/xpenatan/jParser.git")
    scmDeveloperConnection.set("scm:git:ssh://git@github.com/xpenatan/jParser.git")
}
