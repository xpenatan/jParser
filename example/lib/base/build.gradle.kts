dependencies {
    if(LibExt.exampleUseRepoLibs) {
        implementation("com.github.xpenatan.jParser:base:${LibExt.libVersion}")
        implementation("com.github.xpenatan.jParser:core:${LibExt.libVersion}")
    }
    else {
        implementation(project(":jParser:base"))
        implementation(project(":jParser:loader:loader-core"))
    }
}