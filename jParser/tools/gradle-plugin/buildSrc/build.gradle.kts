plugins {
    `kotlin-dsl`
}

repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
}

sourceSets {
    main {
        kotlin {
            srcDir("../../../../buildSrc/src/main/kotlin")
            include("LibExt.kt")
        }
    }
}
