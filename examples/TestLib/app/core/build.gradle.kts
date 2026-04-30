plugins {
    id("java")
}

java {
    sourceCompatibility = JavaVersion.toVersion(LibExt.javaMainTarget)
    targetCompatibility = JavaVersion.toVersion(LibExt.javaMainTarget)
}

dependencies {
    compileOnly(project(":examples:TestLib:lib:lib-core"))

    implementation("com.badlogicgames.gdx:gdx:${LibExt.gdxVersion}")

    testImplementation("junit:junit:${LibExt.jUnitVersion}")
    testImplementation(project(":examples:TestLib:lib:lib-base"))
    testImplementation(project(":examples:TestLib:lib:lib-core"))
    testCompileOnly(project(":idl:runtime:runtime-core"))
}