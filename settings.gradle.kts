include(":jParser:core")
include(":jParser:base")
include(":jParser:idl")
include(":jParser:loader")
include(":jParser:cpp")
include(":jParser:teavm")

include(":example:lib:generator")
include(":example:lib:base")
include(":example:lib:core")
include(":example:lib:desktop")
include(":example:lib:teavm")

include(":example:app:core")
include(":example:app:desktop")
include(":example:app:teavm")

includeBuild("E:\\Dev\\Projects\\java\\gdx-teavm") {
    dependencySubstitution {
        substitute(module("com.github.xpenatan.gdx-teavm:backend-teavm")).using(project(":backends:backend-teavm"))
    }
}