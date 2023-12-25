plugins {
    id("java")
}


dependencies {
    implementation(project(":example:lib:core"))
    implementation("org.teavm:teavm-jso:${LibExt.teaVMVersion}")
    implementation("org.teavm:teavm-classlib:${LibExt.teaVMVersion}")
}

tasks.named("clean") {
    doFirst {
        val srcPath = "$projectDir/src/main/java/gen"
        project.delete(files(srcPath))
    }
}