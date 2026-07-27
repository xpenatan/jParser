pluginManagement {
    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("https://central.sonatype.com/repository/maven-snapshots/")
        }
        gradlePluginPortal()
    }
}

rootProject.name = "jParser"

include(":gradle-plugin")

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

include(":jParser:runtime:base")
include(":jParser:runtime:builder")
include(":jParser:runtime:resources")
include(":jParser:runtime:core")
include(":jParser:runtime:web:runtime-web")
include(":jParser:runtime:shared:runtime-jni")
include(":jParser:runtime:desktop:runtime-desktop-jni")
include(":jParser:runtime:desktop:runtime-desktop-ffm")
include(":jParser:runtime:android:runtime-android")
include(":jParser:runtime:shared:runtime-c")
include(":jParser:runtime:desktop:runtime-desktop-c")
include(":jParser:runtime:android:runtime-android-c")
include(":jParser:runtime:ios:runtime-ios-c")

include(":jParser:loader:loader-core")
include(":jParser:loader:loader-c")
include(":jParser:loader:loader-web")

include(":examples:TestLib:lib:builder")
include(":examples:TestLib:lib:base")
include(":examples:TestLib:lib:core")
include(":examples:TestLib:lib:shared:TestLib-jni")
include(":examples:TestLib:lib:desktop:TestLib-desktop-jni")
include(":examples:TestLib:lib:desktop:TestLib-desktop-ffm")
include(":examples:TestLib:lib:web:TestLib-web")
include(":examples:TestLib:lib:android:TestLib-android")
include(":examples:TestLib:lib:shared:TestLib-c")
include(":examples:TestLib:lib:desktop:TestLib-desktop-c")
include(":examples:TestLib:lib:android:TestLib-android-c")
include(":examples:TestLib:lib:ios:TestLib-ios-c")

include(":examples:TestLib:app:core")
include(":examples:TestLib:app:platforms:desktop-jni")
include(":examples:TestLib:app:platforms:desktop-ffm")
include(":examples:TestLib:app:platforms:desktop-c")
include(":examples:TestLib:app:platforms:ios-c")
include(":examples:TestLib:app:platforms:android-c")
include(":examples:TestLib:app:platforms:web")
include(":examples:TestLib:app:platforms:android")

include(":examples:SharedLib:libA:builder")
include(":examples:SharedLib:libA:base")
include(":examples:SharedLib:libA:core")
include(":examples:SharedLib:libA:shared:LibA-jni")
include(":examples:SharedLib:libA:desktop:LibA-desktop-jni")
include(":examples:SharedLib:libA:desktop:LibA-desktop-ffm")
include(":examples:SharedLib:libA:web:LibA-web")
include(":examples:SharedLib:libA:android:LibA-android")
include(":examples:SharedLib:libA:shared:LibA-c")
include(":examples:SharedLib:libA:desktop:LibA-desktop-c")
include(":examples:SharedLib:libA:android:LibA-android-c")
include(":examples:SharedLib:libA:ios:LibA-ios-c")

include(":examples:SharedLib:libB:builder")
include(":examples:SharedLib:libB:base")
include(":examples:SharedLib:libB:core")
include(":examples:SharedLib:libB:shared:LibB-jni")
include(":examples:SharedLib:libB:desktop:LibB-desktop-jni")
include(":examples:SharedLib:libB:desktop:LibB-desktop-ffm")
include(":examples:SharedLib:libB:web:LibB-web")
include(":examples:SharedLib:libB:android:LibB-android")
include(":examples:SharedLib:libB:shared:LibB-c")
include(":examples:SharedLib:libB:desktop:LibB-desktop-c")
include(":examples:SharedLib:libB:android:LibB-android-c")
include(":examples:SharedLib:libB:ios:LibB-ios-c")

include(":examples:SharedLib:app:core")
include(":examples:SharedLib:app:platforms:desktop-jni")
include(":examples:SharedLib:app:platforms:desktop-ffm")
include(":examples:SharedLib:app:platforms:desktop-bundle-jni")
include(":examples:SharedLib:app:platforms:desktop-bundle-mixed")
include(":examples:SharedLib:app:platforms:desktop-c")
include(":examples:SharedLib:app:platforms:ios-c")
include(":examples:SharedLib:app:platforms:android-c")
include(":examples:SharedLib:app:platforms:web")
include(":examples:SharedLib:app:platforms:android")
include(":examples:SharedLib:bundle")

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
