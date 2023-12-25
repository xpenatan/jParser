plugins {
    id("java-library")
}

dependencies {
    api(project(":example:lib:core"))
    api(project(":example:lib-ext:ext-core"))
}