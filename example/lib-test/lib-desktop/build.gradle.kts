plugins {
    id("java")
}

val libDir = "${projectDir}/../lib-build/build/c++/libs"
val windowsFile = "$libDir/windows/test64.dll"
val linuxFile = "$libDir/linux/libtest64.so"
val macFile = "$libDir/mac/libtest64.dylib"
val macArmFile = "$libDir/mac/arm/libtestarm64.dylib"

tasks.jar {
    from(windowsFile)
    from(linuxFile)
    from(macFile)
    from(macArmFile)
}

dependencies {
    if(LibExt.exampleUseRepoLibs) {
        testImplementation("com.github.xpenatan.jParser:loader-core:${LibExt.libVersion}")
    }
    else {
        testImplementation(project(":jParser:loader:loader-core"))
    }
    testImplementation(project(":example:lib-test:lib-core"))
    testImplementation("junit:junit:${LibExt.jUnitVersion}")
}

tasks.named("clean") {
    doFirst {
        val srcPath = "$projectDir/src/main/"
        project.delete(files(srcPath))
    }
}