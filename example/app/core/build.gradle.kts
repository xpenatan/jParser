dependencies {
    implementation(project(":example:lib:core"))

    implementation("com.badlogicgames.gdx:gdx:${LibExt.gdxVersion}")
}

tasks.build.get().dependsOn(":example:lib:generator:generateNativeProject")
tasks.build.get().mustRunAfter(":example:lib:generator:generateNativeProject")