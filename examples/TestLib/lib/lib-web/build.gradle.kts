plugins {
    id("java")
}

java {
    sourceCompatibility = JavaVersion.toVersion(LibExt.javaWebTarget)
    targetCompatibility = JavaVersion.toVersion(LibExt.javaWebTarget)
}

val emscriptenJS = "$projectDir/../lib-build/build/c++/libs/emscripten/TestLib.js"
val emscriptenWASM = "$projectDir/../lib-build/build/c++/libs/emscripten/TestLib.wasm"

tasks.named("compileJava") {
    dependsOn(":examples:TestLib:lib:plugin:jParser_generate")
}

tasks.jar {
    dependsOn(":examples:TestLib:lib:plugin:jParser_build_web_wasm")
    from(emscriptenJS, emscriptenWASM)
}

dependencies {
    implementation(project(":jParser:loader:loader-web"))

    implementation(project(":jParser:loader:loader-core"))
    implementation(project(":jParser:api:api-web"))
    implementation(project(":jParser:api:api-core"))
    implementation(project(":jParser:runtime:runtime-core"))
    implementation(project(":jParser:runtime:runtime-jvm:web"))
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
