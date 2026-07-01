import org.gradle.api.publish.maven.tasks.PublishToMavenRepository

plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
    `maven-publish`
    signing
}

val moduleName = "jparser-gradle-plugin"

LibExt.configure(rootProject.projectDir)
val taskNames = gradle.startParameter.taskNames
fun isTaskRequested(taskName: String): Boolean {
    return taskNames.any { it == taskName || it.endsWith(":$taskName") }
}

val isPrepareSnapshotDeploy = isTaskRequested("prepareSnapshotDeploy")
val isPrepareReleaseDeploy = isTaskRequested("prepareReleaseDeploy")
val isUploadToMavenCentral = isTaskRequested("uploadToMavenCentral")
val isReleasePublish = isTaskRequested("publishRelease")
val isReleaseIntent = isReleasePublish || isPrepareReleaseDeploy || isUploadToMavenCentral
LibExt.isRelease = isReleaseIntent

group = LibExt.groupId
version = LibExt.libVersion

base {
    archivesName.set(moduleName)
}

dependencies {
    implementation("com.github.xpenatan.jParser:gen-build:${LibExt.libVersion}")
    implementation("com.github.xpenatan.jParser:gen-build-tool:${LibExt.libVersion}")

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
    repositories {
        maven {
            val isSnapshot = LibExt.libVersion.endsWith("-SNAPSHOT")
            val snapshotLocalRepo = File(LibExt.rootDirectory, "build/snapshot-deploy")
            val releaseLocalRepo = File(LibExt.rootDirectory, "build/staging-deploy")
            url = when {
                !isSnapshot -> uri(releaseLocalRepo)
                isPrepareSnapshotDeploy -> uri(snapshotLocalRepo)
                else -> uri("https://central.sonatype.com/repository/maven-snapshots/")
            }
            if(isSnapshot && !isPrepareSnapshotDeploy) {
                val user = System.getenv("CENTRAL_PORTAL_USERNAME")
                val pass = System.getenv("CENTRAL_PORTAL_PASSWORD")
                credentials {
                    username = user
                    password = pass
                }
            }
        }
    }
    publications.withType<MavenPublication>().configureEach {
        groupId = LibExt.groupId
        version = LibExt.libVersion
        if(name == "pluginMaven") {
            artifactId = moduleName
        }
        pom {
            name.set("jParser Gradle plugin")
            description.set("Gradle plugin for jParser generation and native build tasks")
            url.set("http://github.com/xpenatan/jParser")
            developers {
                developer {
                    id.set("Xpe")
                    name.set("Natan")
                }
            }
            scm {
                connection.set("scm:git@github.com:xpenatan/jParser.git")
                developerConnection.set("scm:git@github.com:xpenatan/jParser.git")
                url.set("http://github.com/xpenatan/jParser")
            }
            licenses {
                license {
                    name.set("The Apache License, Version 2.0")
                    url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                }
            }
        }
    }
}

tasks.withType<Javadoc> {
    options.encoding = "UTF-8"
    (options as StandardJavadocDocletOptions).addStringOption("Xdoclint:none", "-quiet")
}

val signingKey = System.getenv("SIGNING_KEY").orEmpty()
val signingPassword = System.getenv("SIGNING_PASSWORD").orEmpty()
if (signingKey.isNotEmpty() && signingPassword.isNotEmpty()) {
    signing {
        useInMemoryPgpKeys(signingKey, signingPassword)
        sign(publishing.publications)
    }
}

tasks.register("prepareSnapshotDeploy") {
    group = "publishing"
    dependsOn(tasks.withType<PublishToMavenRepository>())
    onlyIf { LibExt.libVersion.endsWith("-SNAPSHOT") }
}

tasks.register("prepareReleaseDeploy") {
    group = "publishing"
    dependsOn(tasks.withType<PublishToMavenRepository>())
    onlyIf { !LibExt.libVersion.endsWith("-SNAPSHOT") }
}

tasks.register("publishSnapshot") {
    group = "publishing"
    dependsOn("prepareSnapshotDeploy")
}

tasks.register("publishRelease") {
    group = "publishing"
    dependsOn("prepareReleaseDeploy")
}
