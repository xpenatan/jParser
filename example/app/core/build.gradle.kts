plugins {
    id("java")
}

java {
    sourceCompatibility = JavaVersion.toVersion(LibExt.java8Target)
    targetCompatibility = JavaVersion.toVersion(LibExt.java8Target)
}

dependencies {
    implementation(project(":example:lib:lib-core"))

    implementation("com.badlogicgames.gdx:gdx:${LibExt.gdxVersion}")
}