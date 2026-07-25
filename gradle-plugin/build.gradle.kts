import org.gradle.api.publish.maven.MavenPublication
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
    `maven-publish`
}

val moduleName = "jparser-gradle-plugin"
val pluginId = "com.github.xpenatan.jParser"

base {
    archivesName.set(moduleName)
}

dependencies {
    implementation(project(":jParser:gen:gen-build"))
    implementation(project(":jParser:gen:gen-idl"))
    implementation(project(":jParser:gen:gen-build-tool"))

    testImplementation(gradleTestKit())
    testImplementation(kotlin("test"))
    testImplementation(libs.junit)
}

tasks.test {
    systemProperty(
        "jparser.test.snapshotRepository",
        rootDir.resolve("build/snapshot-deploy").absolutePath
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
