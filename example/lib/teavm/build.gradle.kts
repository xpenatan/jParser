plugins {
    id("java")
}

dependencies {
    implementation("org.teavm:teavm-jso:${LibExt.teaVMVersion}")
    implementation(project(":jParser:loader:loader-teavm"))
    implementation("org.teavm:teavm-classlib:${LibExt.teaVMVersion}")

    if(LibExt.exampleUseRepoLibs) {
        testImplementation("com.github.xpenatan.jParser:loader-core:${LibExt.libVersion}")
    }
    else {
        testImplementation(project(":jParser:loader:loader-core"))
    }
    testImplementation(project(":example:lib:core"))
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