include(":jParser:jParser-core")
include(":jParser:jParser-build")
include(":jParser:jParser-build-tool")
include(":jParser:jParser-base")
include(":jParser:jParser-idl")
include(":jParser:jParser-cpp")
include(":jParser:jParser-teavm")

include(":loader:loader-core")
include(":loader:loader-teavm")

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
//
//includeBuild("E:/Dev/Projects/java/teavm") {
//    dependencySubstitution {
//        substitute(module("org.teavm:teavm-tooling")).using(project(":tools:core"))
//        substitute(module("org.teavm:teavm-core")).using(project(":core"))
//        substitute(module("org.teavm:teavm-classlib")).using(project(":classlib"))
//        substitute(module("org.teavm:teavm-jso")).using(project(":jso:core"))
//        substitute(module("org.teavm:teavm-jso-apis")).using(project(":jso:apis"))
//        substitute(module("org.teavm:teavm-jso-impl")).using(project(":jso:impl"))
//    }
//}