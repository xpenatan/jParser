plugins {
    id("java")
    id("com.github.xpenatan.easy-publishing") version "-SNAPSHOT"
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
LibExt.isRelease = rootProject.extra["easyPublishing.releaseRequested"] as Boolean

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

easyPublishing {
    modules(
        ":jParser:gen:gen-core",
        ":jParser:gen:gen-build",
        ":jParser:gen:gen-build-tool",
        ":jParser:gen:gen-idl",
        ":jParser:gen:gen-jni",
        ":jParser:gen:gen-ffm",
        ":jParser:gen:gen-c",
        ":jParser:gen:gen-web",
        ":jParser:api:api-core",
        ":jParser:api:api-web",
        ":jParser:runtime:base",
        ":jParser:runtime:core",
        ":jParser:runtime:web:runtime-web",
        ":jParser:runtime:shared:runtime-jni",
        ":jParser:runtime:desktop:runtime-desktop-jni",
        ":jParser:runtime:desktop:runtime-desktop-ffm",
        ":jParser:runtime:android:runtime-android",
        ":jParser:runtime:shared:runtime-c",
        ":jParser:runtime:desktop:runtime-desktop-c",
        ":jParser:runtime:android:runtime-android-c",
        ":jParser:loader:loader-core",
        ":jParser:loader:loader-c",
        ":jParser:loader:loader-web"
    )

    groupId.set(LibExt.groupId)
    releaseVersion.set(LibExt.releaseVersion)
    snapshotVersion.set(LibExt.snapshotVersion)

    snapshotRepositoryUrl.set("https://central.sonatype.com/repository/maven-snapshots/")
    releaseRepositoryUrl.set("https://central.sonatype.com")
    username.set(providers.environmentVariable("CENTRAL_PORTAL_USERNAME"))
    password.set(providers.environmentVariable("CENTRAL_PORTAL_PASSWORD"))
    signingKey.set(providers.environmentVariable("SIGNING_KEY"))
    signingPassword.set(providers.environmentVariable("SIGNING_PASSWORD"))

    pomName.set(LibExt.libName)
    pomDescription.set("Java JNI code parser")
    projectUrl.set("https://github.com/xpenatan/jParser")

    developerId.set("Xpe")
    developerName.set("Natan")

    scmUrl.set("https://github.com/xpenatan/jParser")
    scmConnection.set("scm:git:https://github.com/xpenatan/jParser.git")
    scmDeveloperConnection.set("scm:git:ssh://git@github.com/xpenatan/jParser.git")

    nestedBuild("gradle-plugin") {
        directory.set(layout.projectDirectory.dir("jParser/tools/gradle-plugin"))
    }
}

tasks.register("phase3_perf_smoke") {
    group = "verification"
    description = "Run Phase 3 performance smoke benchmark task"
    dependsOn(":jParser:benchmark:benchmark-core:perf_smoke")
}
