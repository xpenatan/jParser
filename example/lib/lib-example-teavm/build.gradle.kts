plugins {
    id("java")
}

dependencies {
    implementation("org.teavm:teavm-jso:0.9.0-dev-7")

    if(LibExt.exampleUseRepoLibs) {
        testImplementation("com.github.xpenatan.jParser:jParser-loader:${LibExt.libVersion}")
    }
    else {
        testImplementation(project(":jParser:loader"))
    }
    testImplementation(project(":example:lib:lib-example-core"))
    testImplementation("junit:junit:${LibExt.jUnitVersion}")
    testImplementation("org.teavm:teavm-core:0.9.0-dev-7")
    testImplementation("org.teavm:teavm-jso-apis:0.9.0-dev-7")
    testImplementation("org.teavm:teavm-classlib:0.9.0-dev-7")
    testImplementation("org.teavm:teavm-junit:0.9.0-dev-7")
}

tasks.named("clean") {
    doFirst {
        val srcPath = "$projectDir/src/main/"
        project.delete(files(srcPath))
    }
}

val tasksOrder = tasks.register<GradleBuild>("prepareTest") {
    tasks = listOf(
            ":example:lib:lib-example-desktop:test",
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