plugins {
    id("java")
    id("java-library")
}

java {
    sourceCompatibility = JavaVersion.toVersion(libs.versions.javaFfm.get())
    targetCompatibility = JavaVersion.toVersion(libs.versions.javaFfm.get())
}

dependencies {
    api(project(":jParser:loader:loader-core"))
    api(project(":jParser:api:api-core"))
    api(project(":jParser:runtime:desktop:runtime-desktop-ffm"))
}

// Bundle FFM-compiled native libraries into the JAR.
val libDir = "${projectDir}/../../builder/build/c++/libs"
val windowsFile = "$libDir/windows/vc/ffm/LibA64.dll"
val linuxFile = "$libDir/linux/ffm/libLibA64.so"
val macFile = "$libDir/mac/ffm/libLibA64.dylib"
val macArmFile = "$libDir/mac/arm/ffm/libLibAarm64.dylib"

tasks.named("compileJava") {
    dependsOn(":examples:SharedLib:libA:builder:LibA_build_project")
}

tasks.jar {
    from(windowsFile)
    from(linuxFile)
    from(macFile)
    from(macArmFile)
}

val fatModeClassesJar = tasks.register<Jar>("fatModeClassesJar") {
    description = "Build the class-only binding artifact used by fat-mode applications."
    archiveClassifier.set("classes")
    from(sourceSets["main"].output)
}

val fatModeClasses by configurations.creating {
    description = "Class-only LibA FFM binding for fat-mode applications."
    isCanBeConsumed = true
    isCanBeResolved = false
}

artifacts {
    add(fatModeClasses.name, fatModeClassesJar)
}

tasks.named("clean") {
    doFirst {
        val srcPath = "$projectDir/src/main/"
        project.delete(files(srcPath))
    }
}
