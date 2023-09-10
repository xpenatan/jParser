dependencies {
    implementation(project(":example:lib:core"))

    implementation("com.badlogicgames.gdx:gdx:${LibExt.gdxVersion}")
}

tasks.compileJava.get().dependsOn(":example:lib:generator:build_project")
tasks.compileJava.get().mustRunAfter(":example:lib:generator:build_project")