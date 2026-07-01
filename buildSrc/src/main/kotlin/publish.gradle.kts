import java.nio.file.Files
import java.nio.file.Paths
import java.net.URLEncoder

var libProjects = mutableSetOf(
    project(":jParser:gen:gen-core"),
    project(":jParser:gen:gen-build"),
    project(":jParser:gen:gen-build-tool"),
    project(":jParser:gen:gen-idl"),
    project(":jParser:gen:gen-jni"),
    project(":jParser:gen:gen-ffm"),
    project(":jParser:gen:gen-c"),
    project(":jParser:gen:gen-web"),
    project(":jParser:api:api-core"),
    project(":jParser:api:api-web"),
    project(":jParser:runtime:runtime-base"),
    project(":jParser:runtime:runtime-core"),
    project(":jParser:runtime:runtime-jvm:web"),
    project(":jParser:runtime:runtime-jvm:jni"),
    project(":jParser:runtime:runtime-jvm:ffm"),
    project(":jParser:runtime:runtime-jvm:android"),
    project(":jParser:runtime:runtime-c:core"),
    project(":jParser:runtime:runtime-c:desktop"),
    project(":jParser:runtime:runtime-c:android"),
    project(":jParser:loader:loader-core"),
    project(":jParser:loader:loader-web")
)

val taskNames = gradle.startParameter.taskNames
fun isTaskRequested(taskName: String): Boolean {
    return taskNames.any { it == taskName || it.endsWith(":$taskName") }
}

val isPrepareSnapshotDeploy = isTaskRequested("prepareSnapshotDeploy")
val isReleasePublish = isTaskRequested("publishRelease")
val isPrepareReleaseDeploy = isTaskRequested("prepareReleaseDeploy")
val isUploadToMavenCentral = isTaskRequested("uploadToMavenCentral")
val isReleaseIntent = isReleasePublish || isPrepareReleaseDeploy || isUploadToMavenCentral
LibExt.isRelease = isReleaseIntent

configure(libProjects) {
    apply(plugin = "signing")
    apply(plugin = "maven-publish")

    if(LibExt.libVersion.isEmpty()) {
        throw RuntimeException("Version cannot be empty")
    }

    extensions.configure<PublishingExtension> {
        repositories {
            maven {
                val isSnapshot = LibExt.libVersion.endsWith("-SNAPSHOT")
                val snapshotLocalRepo = rootProject.layout.buildDirectory.dir("snapshot-deploy").get().asFile
                url = when {
                    !isSnapshot -> uri(rootProject.layout.buildDirectory.dir("staging-deploy"))
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
        publications.configureEach {
            if (this is MavenPublication) {
                pom {
                    name.set(LibExt.libName)
                    description.set("Java JNI code parser")
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
    }

    tasks.withType<Javadoc> {
        options.encoding = "UTF-8"
        (options as StandardJavadocDocletOptions).addStringOption("Xdoclint:none", "-quiet")
    }

    val signingKey = System.getenv("SIGNING_KEY").orEmpty()
    val signingPassword = System.getenv("SIGNING_PASSWORD").orEmpty()
    if (signingKey.isNotEmpty() && signingPassword.isNotEmpty()) {
        extensions.configure<SigningExtension> {
            useInMemoryPgpKeys(signingKey, signingPassword)
            sign(extensions.getByType<PublishingExtension>().publications)
        }
    }
}

val gradlePluginBuildDir = rootProject.layout.projectDirectory.dir("jParser/tools/gradle-plugin").asFile
val prepareGradlePluginSnapshotDeploy = tasks.register<GradleBuild>("prepareGradlePluginSnapshotDeploy") {
    group = "publishing"
    description = "Prepare local snapshot deploy files for the jParser Gradle plugin."
    dir = gradlePluginBuildDir
    tasks = listOf("prepareSnapshotDeploy")
}

val publishGradlePluginSnapshot = tasks.register<GradleBuild>("publishGradlePluginSnapshot") {
    group = "publishing"
    description = "Publish the jParser Gradle plugin snapshot to the configured Maven snapshot repository."
    dependsOn(libProjects.map { it.tasks.withType<PublishToMavenRepository>() })
    dir = gradlePluginBuildDir
    tasks = listOf("publishSnapshot")
}

val prepareGradlePluginReleaseDeploy = tasks.register<GradleBuild>("prepareGradlePluginReleaseDeploy") {
    group = "publishing"
    description = "Prepare local release deploy files for the jParser Gradle plugin."
    dir = gradlePluginBuildDir
    tasks = listOf("prepareReleaseDeploy")
}

tasks.register<Zip>("zipStagingDeploy") {
    dependsOn(libProjects.map { it.tasks.named("publish") })
    dependsOn(prepareGradlePluginReleaseDeploy)
    from(rootProject.layout.buildDirectory.dir("staging-deploy"))
    archiveFileName.set("staging-deploy.zip")
    destinationDirectory.set(rootProject.layout.buildDirectory)
    onlyIf { !LibExt.libVersion.endsWith("-SNAPSHOT") }
}

tasks.register("uploadToMavenCentral") {
    dependsOn("zipStagingDeploy")
    onlyIf { !LibExt.libVersion.endsWith("-SNAPSHOT") }
    doLast {
        // Define paths
        val stagingDir = rootProject.layout.buildDirectory.dir("staging-deploy").get().asFile
        val zipFile = rootProject.layout.buildDirectory.file("staging-deploy.zip").get().asFile

        if (!stagingDir.exists()) {
            throw GradleException("Staging directory $stagingDir does not exist. Ensure the publish task ran successfully.")
        }

        if (!zipFile.exists()) {
            throw GradleException("Zip file ${zipFile.absolutePath} was not created. Check the zip command output.")
        }

        if (!Files.isReadable(Paths.get(zipFile.absolutePath))) {
            throw GradleException("Zip file ${zipFile.absolutePath} is not readable. Check file permissions.")
        }

        val username = System.getenv("CENTRAL_PORTAL_USERNAME") ?: throw GradleException("CENTRAL_PORTAL_USERNAME environment variable not set")
        val password = System.getenv("CENTRAL_PORTAL_PASSWORD") ?: throw GradleException("CENTRAL_PORTAL_PASSWORD environment variable not set")

        val rawBundleName = "${LibExt.libName}-${LibExt.libVersion}"
        val encodedBundleName = URLEncoder.encode(rawBundleName, "UTF-8")

        providers.exec {
            commandLine = listOf(
                "curl",
                "-u",
                "$username:$password",
                "--request",
                "POST",
                "--form",
                "bundle=@${zipFile.absolutePath}",
                "https://central.sonatype.com/api/v1/publisher/upload?name=${encodedBundleName}"
            )
        }.result.get()
    }
}

tasks.register("prepareReleaseDeploy") {
    group = "publishing"
    dependsOn("zipStagingDeploy")
    onlyIf { !LibExt.libVersion.endsWith("-SNAPSHOT") }
}

tasks.register("publishRelease") {
    group = "publishing"
    dependsOn("prepareReleaseDeploy")
    finalizedBy("uploadToMavenCentral")
}

tasks.register("publishSnapshot") {
    group = "publishing"
    dependsOn(libProjects.map { it.tasks.withType<PublishToMavenRepository>() })
    dependsOn(publishGradlePluginSnapshot)
}

tasks.register("prepareSnapshotDeploy") {
    group = "publishing"
    dependsOn(libProjects.map { it.tasks.withType<PublishToMavenRepository>() })
    dependsOn(prepareGradlePluginSnapshotDeploy)
    onlyIf { LibExt.libVersion.endsWith("-SNAPSHOT") }
}
