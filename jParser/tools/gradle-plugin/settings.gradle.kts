pluginManagement {
    repositories {
        google()
        maven {
            url = uri("https://central.sonatype.com/repository/maven-snapshots/")
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("../../../gradle/libs.versions.toml"))
        }
    }

    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven {
            url = uri(rootDir.resolve("../../../build/staging-deploy"))
        }
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

val jParserRoot = rootDir.resolve("../../..").canonicalFile

fun includeJParserProject(path: String, relativePath: String) {
    include(path)
    project(path).projectDir = jParserRoot.resolve(relativePath)
}

fun mapContainerProject(path: String) {
    val relativePath = path.removePrefix(":").replace(':', '/')
    val projectDirectory = rootDir.resolve(".gradle/local-project-containers/$relativePath")
    check(projectDirectory.isDirectory || projectDirectory.mkdirs()) {
        "Unable to create local project container directory: $projectDirectory"
    }
    project(path).projectDir = projectDirectory
}

// This standalone build is developed together with the generator. Include the
// generator projects directly so plugin development uses the current sources.
includeJParserProject(":jParser:api:api-core", "jParser/api/api-core")
includeJParserProject(":jParser:loader:loader-core", "jParser/loader/loader-core")
includeJParserProject(":jParser:runtime:base", "jParser/runtime/base")
includeJParserProject(":jParser:gen:gen-core", "jParser/gen/gen-core")
includeJParserProject(":jParser:gen:gen-idl", "jParser/gen/gen-idl")
includeJParserProject(":jParser:gen:gen-jni", "jParser/gen/gen-jni")
includeJParserProject(":jParser:gen:gen-ffm", "jParser/gen/gen-ffm")
includeJParserProject(":jParser:gen:gen-web", "jParser/gen/gen-web")
includeJParserProject(":jParser:gen:gen-c", "jParser/gen/gen-c")
includeJParserProject(":jParser:gen:gen-build", "jParser/gen/gen-build")
includeJParserProject(":jParser:gen:gen-build-tool", "jParser/gen/gen-build-tool")

mapContainerProject(":jParser")
mapContainerProject(":jParser:api")
mapContainerProject(":jParser:loader")
mapContainerProject(":jParser:runtime")
mapContainerProject(":jParser:gen")
