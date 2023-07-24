dependencies {
    if(LibExt.exampleUseRepoLibs) {
        implementation("com.github.xpenatan.jParser:jParser-base:${LibExt.libVersion}")
    }
    else {
        implementation(project(":jParser:base"))
    }
}