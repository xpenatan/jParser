import java.net.URLEncoder
import java.nio.file.Files
import java.nio.file.Paths

plugins {
    id("java")
    id("maven-publish")
    id("signing")
    id("org.jetbrains.kotlin.android") version "2.2.21" apply false
}

buildscript {
    repositories {
        mavenCentral()
        google()
    }

    val kotlinVersion = "2.1.10"

    dependencies {
        classpath("com.android.tools.build:gradle:8.12.3")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
    }
}

LibExt.configure(rootProject.projectDir)

allprojects()  {
    repositories {
        mavenLocal()
        google()
        mavenCentral()
        maven { url = uri("https://central.sonatype.com/repository/maven-snapshots/") }
        maven { url = uri("https://jitpack.io") }
        maven {
            url = uri("http://teavm.org/maven/repository/")
            isAllowInsecureProtocol = true
        }
    }

    configurations.configureEach {
        // Check for updates every sync
        resolutionStrategy.cacheChangingModulesFor(0, "seconds")
    }
}

tasks.register("phase3_perf_smoke") {
    group = "verification"
    description = "Run Phase 3 performance smoke benchmark task"
    dependsOn(":jParser:benchmark:benchmark-core:perf_smoke")
}

apply(plugin = "publish")
