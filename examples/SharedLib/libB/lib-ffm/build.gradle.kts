plugins {
    id("java")
    id("java-library")
}
// FFM (java.lang.foreign.*) requires JDK 22+ at runtime.
java {
    sourceCompatibility = JavaVersion.toVersion(LibExt.java8Target)
    targetCompatibility = JavaVersion.toVersion(LibExt.java8Target)
}
tasks.withType<JavaCompile> {
    javaCompiler.set(javaToolchains.compilerFor {
        languageVersion.set(JavaLanguageVersion.of(25))
    })
    options.release.set(null as Int?)
}
dependencies {
    implementation(project(":examples:SharedLib:libA:lib-ffm"))
    if(LibExt.exampleUseRepoLibs) {
        api("com.github.xpenatan.jParser:loader-core:-SNAPSHOT")
        api("com.github.xpenatan.jParser:idl-core:-SNAPSHOT")
        api("com.github.xpenatan.jParser:idl-helper-ffm:-SNAPSHOT")
    }
    else {
        api(project(":loader:loader-core"))
        api(project(":idl:idl-core"))
        api(project(":idl-helper:idl-helper-ffm"))
    }
}
tasks.named("clean") {
    doFirst {
        val srcPath = "$projectDir/src/main/"
        project.delete(files(srcPath))
    }
}
