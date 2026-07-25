import com.github.xpenatan.easypublishing.EasyPublishingExtension
import com.github.xpenatan.easypublishing.EasyPublishingPlugin
import org.gradle.api.tasks.bundling.Jar
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

base {
    archivesName.set(moduleName)
}

dependencies {
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
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
    withJavadocJar()
    withSourcesJar()
}

val generatedPluginInfoDir = layout.buildDirectory.dir("generated/sources/jParserPluginInfo/kotlin")
val easyPublishingExtension = extensions.getByType<EasyPublishingExtension>()
val generatedPluginVersion = providers.provider {
    val releaseRequested = extensions.extraProperties
        .get(EasyPublishingPlugin.RELEASE_REQUESTED_EXTRA) as Boolean
    if(releaseRequested) {
        easyPublishingExtension.releaseVersion.get()
    }
    else {
        easyPublishingExtension.snapshotVersion.get()
    }
}

val generateJParserPluginInfo = tasks.register("generateJParserPluginInfo") {
    inputs.property("groupId", libs.versions.jParserGroup)
    inputs.property("version", generatedPluginVersion)
    outputs.dir(generatedPluginInfoDir)

    doLast {
        val groupId = libs.versions.jParserGroup.get()
        val version = generatedPluginVersion.get()
        val outputFile = generatedPluginInfoDir.get()
            .file("com/github/xpenatan/jParser/gradle/JParserPluginInfo.kt")
            .asFile
        outputFile.parentFile.mkdirs()
        outputFile.writeText(
            """
            package com.github.xpenatan.jParser.gradle

            internal object JParserPluginInfo {
                const val GROUP = "$groupId"
                const val VERSION = "$version"
            }
            """.trimIndent() + "\n"
        )
    }
}

kotlin {
    sourceSets.named("main") {
        kotlin.srcDir(generatedPluginInfoDir)
    }
}

tasks.withType<KotlinCompile>().configureEach {
    dependsOn(generateJParserPluginInfo)
    compilerOptions.jvmTarget.set(JvmTarget.JVM_17)
}

tasks.withType<Jar>().matching { it.name == "sourcesJar" }.configureEach {
    dependsOn(generateJParserPluginInfo)
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
