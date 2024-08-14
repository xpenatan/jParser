include(":jParser:core")
include(":jParser:builder")
include(":jParser:builder-tool")
include(":jParser:base")
include(":jParser:idl")
include(":jParser:cpp")
include(":jParser:teavm")
include(":jParser:loader:loader-core")
include(":jParser:loader:loader-teavm")

include(":example:lib:lib-build")
include(":example:lib:lib-base")
include(":example:lib:lib-core")
include(":example:lib:lib-desktop")
include(":example:lib:lib-teavm")
include(":example:lib:lib-android")

//include(":example:lib-ext:ext-base")
//include(":example:lib-ext:ext-build")
//include(":example:lib-ext:ext-core")
//include(":example:lib-ext:ext-teavm")

include(":example:app:core")
include(":example:app:desktop")
include(":example:app:teavm")
include(":example:app:android")

//includeBuild("E:\\Dev\\Projects\\java\\gdx-teavm") {
//    dependencySubstitution {
//        substitute(module("com.github.xpenatan.gdx-teavm:backend-teavm")).using(project(":backends:backend-teavm"))
//        substitute(module("com.github.xpenatan.gdx-teavm:asset-loader")).using(project(":extensions:asset-loader"))
//    }
//}