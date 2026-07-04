pluginManagement {
    includeBuild("jParser/tools/gradle-plugin")
    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("https://central.sonatype.com/repository/maven-snapshots/")
        }
        gradlePluginPortal()
    }
}

include(":jParser:gen:gen-core")
include(":jParser:gen:gen-build")
include(":jParser:gen:gen-build-tool")
include(":jParser:gen:gen-idl")
include(":jParser:gen:gen-jni")
include(":jParser:gen:gen-web")
include(":jParser:gen:gen-ffm")
include(":jParser:gen:gen-c")

include(":jParser:api:api-core")
include(":jParser:api:api-web")
include(":jParser:benchmark:benchmark-core")

include(":jParser:runtime:runtime-base")
include(":jParser:runtime:runtime-build")
include(":jParser:runtime:runtime-core")
include(":jParser:runtime:runtime-jvm:web")
include(":jParser:runtime:runtime-jvm:jni")
include(":jParser:runtime:runtime-jvm:ffm")
include(":jParser:runtime:runtime-jvm:android")
include(":jParser:runtime:runtime-c:core")
include(":jParser:runtime:runtime-c:desktop")
include(":jParser:runtime:runtime-c:android")
include(":jParser:runtime:plugin")

include(":jParser:loader:loader-core")
include(":jParser:loader:loader-web")

include(":examples:TestLib:lib:builder")
include(":examples:TestLib:lib:base")
include(":examples:TestLib:lib:core")
include(":examples:TestLib:lib:shared:jni")
include(":examples:TestLib:lib:desktop:ffm")
include(":examples:TestLib:lib:web:wasm")
include(":examples:TestLib:lib:android:jni")
include(":examples:TestLib:lib:shared:c")
include(":examples:TestLib:lib:desktop:c")
include(":examples:TestLib:lib:android:c")
include(":examples:TestLib:lib:plugin")

include(":examples:TestLib:app:core")
include(":examples:TestLib:app:platforms:desktop-jni")
include(":examples:TestLib:app:platforms:desktop-ffm")
include(":examples:TestLib:app:platforms:desktop-c")
include(":examples:TestLib:app:platforms:android-c")
include(":examples:TestLib:app:platforms:web")
include(":examples:TestLib:app:platforms:android")

include(":examples:SharedLib:libA:builder")
include(":examples:SharedLib:libA:base")
include(":examples:SharedLib:libA:core")
include(":examples:SharedLib:libA:shared:jni")
include(":examples:SharedLib:libA:desktop:ffm")
include(":examples:SharedLib:libA:web:wasm")
include(":examples:SharedLib:libA:android:jni")
include(":examples:SharedLib:libA:shared:c")
include(":examples:SharedLib:libA:desktop:c")
include(":examples:SharedLib:libA:android:c")
include(":examples:SharedLib:libA:plugin")

include(":examples:SharedLib:libB:builder")
include(":examples:SharedLib:libB:base")
include(":examples:SharedLib:libB:core")
include(":examples:SharedLib:libB:shared:jni")
include(":examples:SharedLib:libB:desktop:ffm")
include(":examples:SharedLib:libB:web:wasm")
include(":examples:SharedLib:libB:android:jni")
include(":examples:SharedLib:libB:shared:c")
include(":examples:SharedLib:libB:desktop:c")
include(":examples:SharedLib:libB:android:c")
include(":examples:SharedLib:libB:plugin")

include(":examples:SharedLib:app:core")
include(":examples:SharedLib:app:platforms:desktop-jni")
include(":examples:SharedLib:app:platforms:desktop-ffm")
include(":examples:SharedLib:app:platforms:desktop-c")
include(":examples:SharedLib:app:platforms:android-c")
include(":examples:SharedLib:app:platforms:web")
include(":examples:SharedLib:app:platforms:android")

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
