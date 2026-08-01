pluginManagement {
    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("https://central.sonatype.com/repository/maven-snapshots/")
        }
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }

    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven {
            url = uri(rootDir.resolve("../build/snapshot-deploy"))
        }
        maven {
            url = uri(rootDir.resolve("../build/staging-deploy"))
        }
        mavenLocal()
        google()
        mavenCentral()
        maven {
            url = uri("https://central.sonatype.com/repository/maven-snapshots/")
        }
        maven {
            url = uri("http://teavm.org/maven/repository/")
            isAllowInsecureProtocol = true
        }
    }
}

rootProject.name = "gradle-plugin"

val generatorProjects = listOf(
    "gen-core",
    "gen-build",
    "gen-build-tool",
    "gen-idl",
    "gen-jni",
    "gen-web",
    "gen-ffm",
    "gen-c"
)

generatorProjects.forEach { projectName ->
    include(":$projectName")
    project(":$projectName").projectDir = file("../jParser/gen/$projectName")
}
