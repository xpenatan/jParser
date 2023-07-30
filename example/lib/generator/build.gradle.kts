plugins {
    id("java")
    id("net.freudasoft.gradle-cmake-plugin") version("0.0.2")
}

val mainClassName = "Main"

dependencies {
    implementation(project(":example:lib:base"))
    if(LibExt.exampleUseRepoLibs) {
        implementation("com.github.xpenatan.jParser:jParser-core:${LibExt.libVersion}")
        implementation("com.github.xpenatan.jParser:jParser-idl:${LibExt.libVersion}")
        implementation("com.github.xpenatan.jParser:jParser-teavm:${LibExt.libVersion}")
        implementation("com.github.xpenatan.jParser:jParser-cpp:${LibExt.libVersion}")
    }
    else {
        implementation(project(":jParser:core"))
        implementation(project(":jParser:idl"))
        implementation(project(":jParser:teavm"))
        implementation(project(":jParser:cpp"))
    }
}

tasks.register<JavaExec>("generateNativeProject") {
    group = "gen"
    description = "Generate native project"
    mainClass.set(mainClassName)
    classpath = sourceSets["main"].runtimeClasspath
    dependsOn("clean")
    mustRunAfter("clean")
}

cmake {
    generator.set("MinGW Makefiles")

    sourceFolder.set(file("$projectDir/src/main/cpp"))

    buildConfig.set("Release")
    buildTarget.set("install")
    buildClean.set(true)
}

tasks.register("buildEmscripten") {
    dependsOn("cmakeBuild")
    mustRunAfter("cmakeBuild")
    group = "gen"
    description = "Generate javascript"

    doLast {
        copy{
            from(
                "$buildDir/cmake/exampleLib.js",
                "$buildDir/cmake/exampleLib.wasm.js"
            )
            into("$projectDir/../teavm/src/main/resources")
        }
    }
}