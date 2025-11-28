plugins {
    id("java")
}

java {
    sourceCompatibility = JavaVersion.toVersion(LibExt.java11Target)
    targetCompatibility = JavaVersion.toVersion(LibExt.java11Target)
}

val emscriptenJS = "$projectDir/../lib-build/build/c++/libs/emscripten/TestLib.js"
val emscriptenWASM = "$projectDir/../lib-build/build/c++/libs/emscripten/TestLib.wasm"

tasks.jar {
    from(emscriptenJS, emscriptenWASM)
}

dependencies {
    implementation("org.teavm:teavm-jso:${LibExt.teaVMVersion}")
    implementation("org.teavm:teavm-classlib:${LibExt.teaVMVersion}")

    if(LibExt.exampleUseRepoLibs) {
        implementation("com.github.xpenatan.jParser:loader-teavm:-SNAPSHOT")
        implementation("com.github.xpenatan.jParser:loader-core:-SNAPSHOT")
        implementation("com.github.xpenatan.jParser:idl-teavm:-SNAPSHOT")
        implementation("com.github.xpenatan.jParser:idl-core:-SNAPSHOT")
    }
    else {
        implementation(project(":loader:loader-teavm"))
        implementation(project(":loader:loader-core"))
        implementation(project(":idl:idl-teavm"))
        implementation(project(":idl:idl-core"))
    }
//    testImplementation(project(":example:lib:lib-core"))
//    testImplementation("junit:junit:${LibExt.jUnitVersion}")
//    testImplementation("org.teavm:teavm-core:${LibExt.teaVMVersion}")
//    testImplementation("org.teavm:teavm-jso-apis:${LibExt.teaVMVersion}")
//    testImplementation("org.teavm:teavm-classlib:${LibExt.teaVMVersion}")
//    testImplementation("org.teavm:teavm-junit:${LibExt.teaVMVersion}")
}

tasks.named("clean") {
    doFirst {
        val srcPath = "$projectDir/src/main/java"
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