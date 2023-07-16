plugins {
    id("java")
    id("maven-publish")
    id("signing")
}

buildscript {
    repositories {
        mavenCentral()
    }

    dependencies {
    }
}

allprojects  {
    apply {
        plugin("java")
        plugin("maven-publish")
    }
    apply(plugin = "maven-publish")

    java.sourceCompatibility = JavaVersion.VERSION_11
    java.targetCompatibility = JavaVersion.VERSION_11

    repositories {
        mavenLocal()
        mavenCentral()
        maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots/") }
        maven { url = uri("https://oss.sonatype.org/content/repositories/releases/") }
    }
}

var libProjects = mutableSetOf( project(":jParser:core"),
    project(":jParser:idl"),
    project(":jParser:loader"),
    project(":jParser:cpp"),
    project(":jParser:teavm"))

configure(libProjects) {
    apply(plugin = "signing")

    group = LibExt.groupId
    version = LibExt.libVersion

    publishing {
        repositories {
            maven {
                url = uri {
                    val ver = project.version.toString()
                    val isSnapshot = ver.toUpperCase().contains("SNAPSHOT")
                    val repoUrl = "https://oss.sonatype.org/service/local/staging/deploy/maven2/"
                    val repoUrlSnapshot = "https://oss.sonatype.org/content/repositories/snapshots/"
                    if(isSnapshot) repoUrlSnapshot else repoUrl
                }
                credentials {
                    username = System.getenv("USER")
                    password = System.getenv("PASSWORD")
                }
            }
        }
    }

//    javadoc {
//        options.encoding = 'UTF-8'
//        options.addStringOption('Xdoclint:none', '-quiet')
//    }
////
//    java {
//        withJavadocJar()
//        withSourcesJar()
//    }

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