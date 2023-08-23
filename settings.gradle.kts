include(":jParser:core")
include(":jParser:builder")
include(":jParser:base")
include(":jParser:idl")
include(":jParser:cpp")
include(":jParser:teavm")
include(":jParser:loader:loader-core")
include(":jParser:loader:loader-teavm")

include(":example:lib:generator")
include(":example:lib:base")
include(":example:lib:core")
include(":example:lib:desktop")
include(":example:lib:teavm")
include(":example:lib:android")

include(":example:app:core")
include(":example:app:desktop")
include(":example:app:teavm")
include(":example:app:android")


//includeBuild("E:\\Dev\\Projects\\java\\gdx-teavm") {
//    dependencySubstitution {
//        substitute(module("com.github.xpenatan.gdx-teavm:backend-teavm")).using(project(":backends:backend-teavm"))
//    }
//}