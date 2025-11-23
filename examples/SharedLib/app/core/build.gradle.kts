plugins {
    id("java")
}

java {
    sourceCompatibility = JavaVersion.toVersion(LibExt.java8Target)
    targetCompatibility = JavaVersion.toVersion(LibExt.java8Target)
}

dependencies {
    implementation(project(":examples:SharedLib:libA:lib-core"))

    implementation("com.badlogicgames.gdx:gdx:${LibExt.gdxVersion}")
}