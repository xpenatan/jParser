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
val publishedModules = listOf(
    ":",
    ":gen-core",
    ":gen-build",
    ":gen-build-tool",
    ":gen-idl",
    ":gen-jni",
    ":gen-ffm",
    ":gen-c",
    ":gen-web"
)

allprojects {
    configurations.configureEach {
        resolutionStrategy.cacheChangingModulesFor(0, "seconds")
    }
}

base {
    archivesName.set(moduleName)
}

dependencies {
    implementation(project(":gen-build"))
    implementation(project(":gen-idl"))
    implementation(project(":gen-build-tool"))

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
    modules(*publishedModules.toTypedArray())

    groupId.set(libs.versions.jParserGroup)
    releaseVersion.set(libs.versions.jParserRelease)
    snapshotVersion.set(libs.versions.jParserSnapshot)

    snapshotRepositoryUrl.set("https://central.sonatype.com/repository/maven-snapshots/")
    releaseRepositoryUrl.set("https://central.sonatype.com")
    username.set(providers.environmentVariable("CENTRAL_PORTAL_USERNAME"))
    password.set(providers.environmentVariable("CENTRAL_PORTAL_PASSWORD"))
    signingKey.set(providers.environmentVariable("SIGNING_KEY"))
    signingPassword.set(providers.environmentVariable("SIGNING_PASSWORD"))

    pomName.set(libs.versions.jParserName)
    pomDescription.set("Java JNI code parser")
    projectUrl.set("https://github.com/xpenatan/jParser")

    developerId.set("Xpe")
    developerName.set("Natan")

    scmUrl.set("https://github.com/xpenatan/jParser")
    scmConnection.set("scm:git:https://github.com/xpenatan/jParser.git")
    scmDeveloperConnection.set("scm:git:ssh://git@github.com/xpenatan/jParser.git")
}
