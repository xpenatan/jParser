include(":jParser:gen:gen-core")
include(":jParser:gen:gen-build")
include(":jParser:gen:gen-build-tool")
include(":jParser:gen:gen-idl")
include(":jParser:gen:gen-jni")
include(":jParser:gen:gen-web")
include(":jParser:gen:gen-ffm")

include(":jParser:api:api-core")
include(":jParser:api:api-web")
include(":jParser:benchmark:benchmark-core")

include(":jParser:runtime:runtime-base")
include(":jParser:runtime:runtime-build")
include(":jParser:runtime:runtime-core")
include(":jParser:runtime:runtime-web")
include(":jParser:runtime:runtime-jni")
include(":jParser:runtime:runtime-ffm")
include(":jParser:runtime:runtime-android")

include(":jParser:loader:loader-core")
include(":jParser:loader:loader-web")

include(":examples:TestLib:lib:lib-build")
include(":examples:TestLib:lib:lib-base")
include(":examples:TestLib:lib:lib-core")
include(":examples:TestLib:lib:lib-jni")
include(":examples:TestLib:lib:lib-ffm")
include(":examples:TestLib:lib:lib-web")
include(":examples:TestLib:lib:lib-android")

include(":examples:TestLib:app:core")
include(":examples:TestLib:app:desktop-jni")
include(":examples:TestLib:app:desktop-ffm")
include(":examples:TestLib:app:web")
include(":examples:TestLib:app:android")

include(":examples:SharedLib:libA:lib-build")
include(":examples:SharedLib:libA:lib-base")
include(":examples:SharedLib:libA:lib-core")
include(":examples:SharedLib:libA:lib-jni")
include(":examples:SharedLib:libA:lib-ffm")
include(":examples:SharedLib:libA:lib-web")
include(":examples:SharedLib:libA:lib-android")

include(":examples:SharedLib:libB:lib-build")
include(":examples:SharedLib:libB:lib-base")
include(":examples:SharedLib:libB:lib-core")
include(":examples:SharedLib:libB:lib-jni")
include(":examples:SharedLib:libB:lib-ffm")
include(":examples:SharedLib:libB:lib-web")
include(":examples:SharedLib:libB:lib-android")

include(":examples:SharedLib:app:core")
include(":examples:SharedLib:app:desktop-jni")
include(":examples:SharedLib:app:desktop-ffm")
include(":examples:SharedLib:app:web")
include(":examples:SharedLib:app:android")

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