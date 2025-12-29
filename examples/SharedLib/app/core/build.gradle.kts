plugins {
    id("java")
}

java {
    sourceCompatibility = JavaVersion.toVersion(LibExt.java8Target)
    targetCompatibility = JavaVersion.toVersion(LibExt.java8Target)
}

dependencies {
    implementation(project(":examples:SharedLib:libA:lib-core"))
    implementation(project(":examples:SharedLib:libB:lib-core"))

    implementation("com.badlogicgames.gdx:gdx:${LibExt.gdxVersion}")

    testImplementation(project(":examples:SharedLib:libA:lib-desktop"))
    testImplementation(project(":examples:SharedLib:libB:lib-desktop"))
    testImplementation(project(":idl-helper:idl-helper-desktop"))
    testImplementation("junit:junit:${LibExt.jUnitVersion}")
}