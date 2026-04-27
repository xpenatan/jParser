include(":jParser:jParser-core")
include(":jParser:jParser-build")
include(":jParser:jParser-build-tool")
include(":jParser:jParser-base")
include(":jParser:jParser-idl")
include(":jParser:jParser-jni")
include(":jParser:jParser-teavm")
include(":jParser:jParser-ffm")

include(":idl:api:api-core")
include(":idl:api:api-teavm")

include(":idl:runtime:runtime-base")
include(":idl:runtime:runtime-build")
include(":idl:runtime:runtime-core")
include(":idl:runtime:runtime-teavm")
include(":idl:runtime:runtime-desktop-jni")
include(":idl:runtime:runtime-desktop-ffm")
include(":idl:runtime:runtime-android")

include(":loader:loader-core")
include(":loader:loader-teavm")

include(":examples:TestLib:lib:lib-build")
include(":examples:TestLib:lib:lib-base")
include(":examples:TestLib:lib:lib-core")
include(":examples:TestLib:lib:lib-desktop-jni")
include(":examples:TestLib:lib:lib-desktop-ffm")
include(":examples:TestLib:lib:lib-teavm")
include(":examples:TestLib:lib:lib-android")

include(":examples:TestLib:app:core")
include(":examples:TestLib:app:desktop-jni")
include(":examples:TestLib:app:desktop-ffm")
include(":examples:TestLib:app:teavm")
include(":examples:TestLib:app:android")

include(":examples:SharedLib:libA:lib-build")
include(":examples:SharedLib:libA:lib-base")
include(":examples:SharedLib:libA:lib-core")
include(":examples:SharedLib:libA:lib-desktop-jni")
include(":examples:SharedLib:libA:lib-desktop-ffm")
include(":examples:SharedLib:libA:lib-teavm")
include(":examples:SharedLib:libA:lib-android")

include(":examples:SharedLib:libB:lib-build")
include(":examples:SharedLib:libB:lib-base")
include(":examples:SharedLib:libB:lib-core")
include(":examples:SharedLib:libB:lib-desktop-jni")
include(":examples:SharedLib:libB:lib-desktop-ffm")
include(":examples:SharedLib:libB:lib-teavm")
include(":examples:SharedLib:libB:lib-android")

include(":examples:SharedLib:app:core")
include(":examples:SharedLib:app:desktop-jni")
include(":examples:SharedLib:app:desktop-ffm")
include(":examples:SharedLib:app:teavm")
include(":examples:SharedLib:app:android")

//include(":example:lib-ext:ext-base")
//include(":example:lib-ext:ext-build")
//include(":example:lib-ext:ext-core")
//include(":example:lib-ext:ext-teavm")


//includeBuild("E:\\Dev\\Projects\\java\\gdx-teavm") {
//    dependencySubstitution {
//        substitute(module("com.github.xpenatan.gdx-teavm:backend-shared")).using(project(":backends:backend-shared"))
//        substitute(module("com.github.xpenatan.gdx-teavm:backend-web")).using(project(":backends:backend-web"))
//        substitute(module("com.github.xpenatan.gdx-teavm:asset-loader")).using(project(":extensions:asset-loader"))
//        substitute(module("com.github.xpenatan.gdx-teavm:gdx-freetype-teavm")).using(project(":extensions:gdx-freetype-teavm"))
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
//        substitute(module("org.teavm:teavm-gradle-plugin")).using(project(":tools:gradle"))
//    }
//}