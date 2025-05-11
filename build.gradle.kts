plugins {
    id("java")
    id("maven-publish")
    id("signing")
    id("org.jetbrains.kotlin.android") version "1.8.21" apply false
}

buildscript {
    repositories {
        mavenCentral()
        google()
    }

    val kotlinVersion = "2.1.10"

    dependencies {
        classpath("com.android.tools.build:gradle:8.0.1")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
    }
}

allprojects()  {
    apply {
        plugin("maven-publish")
    }
    apply(plugin = "maven-publish")

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

configure(allprojects
        - project(":example:app:android")
        - project(":example:lib:lib-android")
) {
    apply {
        plugin("java")
    }
    java.sourceCompatibility = JavaVersion.VERSION_11
    java.targetCompatibility = JavaVersion.VERSION_11
}

var libProjects = mutableSetOf(
    project(":jParser:core"),
    project(":jParser:builder"),
    project(":jParser:builder-tool"),
    project(":jParser:base"),
    project(":jParser:idl"),
    project(":jParser:loader:loader-core"),
    project(":jParser:loader:loader-teavm"),
    project(":jParser:cpp"),
    project(":jParser:teavm")
)

configure(libProjects) {
    apply(plugin = "signing")

    group = LibExt.groupId
    version = LibExt.libVersion

    publishing {
        repositories {
            maven {
                url = if (project.version.toString().endsWith("-SNAPSHOT")) {
                    uri("https://central.sonatype.com/repository/maven-snapshots/")
                } else {
                    uri("https://ossrh-staging-api.central.sonatype.com/service/local/staging/deploy/maven2/")
                }
                credentials {
                    username = System.getenv("CENTRAL_PORTAL_USERNAME")
                    password = System.getenv("CENTRAL_PORTAL_PASSWORD")
                }
            }
        }
    }

    tasks.withType<Javadoc> {
        options.encoding = "UTF-8"
        (options as StandardJavadocDocletOptions).addStringOption("Xdoclint:none", "-quiet")
    }

    java {
        withJavadocJar()
        withSourcesJar()
    }

    publishing.publications.configureEach {
        if (this is MavenPublication) {
            pom {
                name.set("jParser")
                description.set("Java JNI code parser")
                url.set("http://github.com/xpenatan/jParser")
                developers {
                    developer {
                        id.set("Xpe")
                        name.set("Natan")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/xpenatan/jParser.git")
                    developerConnection.set("scm:git:ssh://github.com/xpenatan/jParser.git")
                    url.set("http://github.com/xpenatan/jParser/tree/master")
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

    val signingKey = System.getenv("SIGNING_KEY")
    val signingPassword = System.getenv("SIGNING_PASSWORD")
    if(signingKey != null && signingPassword != null) {
        signing {
            useInMemoryPgpKeys(signingKey, signingPassword)
            publishing.publications.configureEach {
                sign(this)
            }
        }
    }
}