plugins {
    id("java")
}

dependencies {
    implementation(project(":jParser:core"))
    implementation(project(":jParser:idl"))
    implementation(project(":jParser:teavm"))
    implementation(project(":jParser:cpp"))
    implementation(project(":jParser:builder"))
}