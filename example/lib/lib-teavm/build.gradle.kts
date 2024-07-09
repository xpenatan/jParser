plugins {
    id("java")
}

val emscriptenFile = "$projectDir/../lib-build/build/c++/libs/emscripten/exampleLib.wasm.js"
val emscriptenSideFile = "$projectDir/../lib-build/build/c++/libs/emscripten/exampleLibside.wasm"

tasks.jar {
    from(emscriptenFile, emscriptenSideFile)
}


dependencies {
    implementation("org.teavm:teavm-jso:${LibExt.teaVMVersion}")
    implementation(project(":jParser:loader:loader-teavm"))
    implementation("org.teavm:teavm-classlib:${LibExt.teaVMVersion}")

    if(LibExt.exampleUseRepoLibs) {
        implementation("com.github.xpenatan.jParser:loader-core:${LibExt.libVersion}")
    }
    else {
        implementation(project(":jParser:loader:loader-core"))
    }
    testImplementation(project(":example:lib:lib-core"))
    testImplementation("junit:junit:${LibExt.jUnitVersion}")
    testImplementation("org.teavm:teavm-core:${LibExt.teaVMVersion}")
    testImplementation("org.teavm:teavm-jso-apis:${LibExt.teaVMVersion}")
    testImplementation("org.teavm:teavm-classlib:${LibExt.teaVMVersion}")
    testImplementation("org.teavm:teavm-junit:${LibExt.teaVMVersion}")
}

tasks.named("clean") {
    doFirst {
        val srcPath = "$projectDir/src/main/java/gen"
        project.delete(files(srcPath))
    }
}

val tasksOrder = tasks.register<GradleBuild>("prepareTest") {
    tasks = listOf(
            ":example:lib:desktop:prepareTest",
            "compileTestJava"
    )
}

tasks.named("test") {
    dependsOn(tasksOrder)
    mustRunAfter(tasksOrder)
}

tasks.test {
    systemProperty("teavm.junit.target", "${project.buildDir.absolutePath }/js-tests")
    systemProperty("teavm.junit.js.runner", "browser-firefox")
//    systemProperty("teavm.junit.js.runner", "browser")
    systemProperty("teavm.junit.threads", "1")
    systemProperty("teavm.junit.minified", false)
    systemProperty("teavm.junit.optimized", false)
    systemProperty("teavm.junit.js.decodeStack", false)
    maxParallelForks = (Runtime.getRuntime().availableProcessors() / 2).coerceAtLeast(1)
}