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

include(":example:lib-test:lib-build")
include(":example:lib-test:lib-base")
include(":example:lib-test:lib-core")
include(":example:lib-test:lib-desktop")
include(":example:lib-test:lib-teavm")
include(":example:lib-test:lib-android")

//include(":example:lib-ext:ext-base")
//include(":example:lib-ext:ext-build")
//include(":example:lib-ext:ext-core")
//include(":example:lib-ext:ext-teavm")

include(":example:app:core")
include(":example:app:desktop")
include(":example:app:teavm")
include(":example:app:android")

include(":example:app-test:core")
include(":example:app-test:desktop")
include(":example:app-test:teavm")
include(":example:app-test:android")


//includeBuild("E:\\Dev\\Projects\\java\\gdx-teavm") {
//    dependencySubstitution {
//        substitute(module("com.github.xpenatan.gdx-teavm:backend-teavm")).using(project(":backends:backend-teavm"))
//        substitute(module("com.github.xpenatan.gdx-teavm:asset-loader")).using(project(":extensions:asset-loader"))
//    }
//}