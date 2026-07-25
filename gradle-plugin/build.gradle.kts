import org.gradle.api.publish.maven.MavenPublication
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
    alias(libs.plugins.easyPublishing)
}

val moduleName = "jparser-gradle-plugin"
val pluginId = "com.github.xpenatan.jParser"
val generatorModules = listOf("gen-build", "gen-idl", "gen-build-tool")
val generatorVersion = providers.provider { project.version.toString() }

base {
    archivesName.set(moduleName)
}

dependencies {
    generatorModules.forEach { generatorModule ->
        implementation(
            generatorVersion.map { version ->
                "${libs.versions.jParserGroup.get()}:$generatorModule:$version"
            }
        )
    }

    testImplementation(gradleTestKit())
    testImplementation(kotlin("test"))
    testImplementation(libs.junit)
}

tasks.test {
    systemProperty(
        "jparser.test.snapshotRepository",
        rootDir.resolve("../build/snapshot-deploy").absolutePath
    )
}

java {
    sourceCompatibility = JavaVersion.toVersion(libs.versions.javaFfm.get())
    targetCompatibility = JavaVersion.toVersion(libs.versions.javaFfm.get())
    withJavadocJar()
    withSourcesJar()
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions.jvmTarget.set(JvmTarget.fromTarget(libs.versions.javaFfm.get()))
}

gradlePlugin {
    plugins {
        create("jParser") {
            id = pluginId
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
    groupId.set(libs.versions.jParserGroup)
    releaseVersion.set(libs.versions.jParserRelease)
    snapshotVersion.set(libs.versions.jParserSnapshot)

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
