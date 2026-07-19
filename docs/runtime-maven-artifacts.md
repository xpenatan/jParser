# Runtime Maven Artifact Strategy

This document explains the Maven artifact setup used by jParser runtime modules so external libraries can replicate it exactly.

## Module Layout

Top-level runtime modules:

- `jParser/runtime/base`
- `jParser/runtime/builder`
- `jParser/runtime/core`
- `jParser/runtime/shared/runtime-c`
- `jParser/runtime/desktop/runtime-desktop-jni`
- `jParser/runtime/desktop/runtime-desktop-c`
- `jParser/runtime/android/runtime-android-c`

Runtime implementation modules:

- `jParser/runtime/shared/runtime-jni`
- `jParser/runtime/desktop/runtime-desktop-jni`
- `jParser/runtime/desktop/runtime-desktop-ffm`
- `jParser/runtime/web/runtime-web`
- `jParser/runtime/android/runtime-android`
- `jParser/runtime/android/runtime-android-c`

Gradle project paths for split modules use the same artifact-style leaf names as the folders, for example `:jParser:runtime:shared:runtime-jni`, `:jParser:runtime:desktop:runtime-desktop-jni`, `:jParser:runtime:desktop:runtime-desktop-ffm`, and `:jParser:runtime:web:runtime-web`.

## Published Artifact Names

Existing JVM/web artifacts keep their names:

- `runtime-core`
- `runtime-jni`
- `runtime-desktop-ffm`
- `runtime-web`
- `runtime-android`

Desktop JNI artifacts use separate modules for generated Java and platform native payloads:

- `runtime-jni`
- `runtime-desktop-jni`
- `runtime-desktop-jni_windows_x64`
- `runtime-desktop-jni_linux_x64`
- `runtime-desktop-jni_mac_x64`
- `runtime-desktop-jni_mac_arm64`

Android JNI native payloads are split by ABI:

- `runtime-android_x86`
- `runtime-android_x86_64`
- `runtime-android_armeabi_v7a`
- `runtime-android_arm64_v8a`

The main `runtime-jni` artifact is published from `jParser/runtime/shared/runtime-jni` and contains generated/shared JNI Java classes only. Desktop JNI publishes `runtime-desktop-jni` from `jParser/runtime/desktop/runtime-desktop-jni` as the desktop entry jar with a dependency on `runtime-jni`, plus native split jars for each platform. Android JNI publishes `runtime-android` from `jParser/runtime/android/runtime-android` as the Android entry AAR with a dependency on `runtime-jni`, plus native split AARs for each ABI.

TeaVM C artifacts use separate modules for generated Java and platform native payloads:

- `runtime-c`
- `runtime-desktop-c_windows_x64`
- `runtime-desktop-c_linux_x64`
- `runtime-desktop-c_mac_x64`
- `runtime-desktop-c_mac_arm64`
- `runtime-android-c`
- `runtime-android-c_x86`
- `runtime-android-c_x86_64`
- `runtime-android-c_armeabi_v7a`
- `runtime-android-c_arm64_v8a`

The main `runtime-c` artifact is published from `jParser/runtime/shared/runtime-c`. `runtime-core` remains its public API; `runtime-c` exposes that artifact and contributes the generated `gen.c.*` implementations, TeaVM C substitution service, glue/import headers, and portable CMake resources. Desktop native split artifacts are published from `jParser/runtime/desktop/runtime-desktop-c`. The Windows split contains both MT and MD static/shared payload sets; other desktop splits contain their platform payloads. Every split also carries a gdx-teavm discovery marker. Android TeaVM C publishes a main `runtime-android-c` AAR from `jParser/runtime/android/runtime-android-c` with a dependency on `runtime-c`, plus native split AARs for each ABI.

`jParser/runtime/android/runtime-android-c` builds local native-only AAR files with the `runtime-android-c-<abi>` archive base name. Maven publication uses the public artifact IDs `runtime-android-c_<abi>`.

## Local vs Publish Behavior

Desktop JNI (`runtime-desktop-jni`), desktop FFM, and web (`runtime-web`) keep local project dependencies convenient by adding native/web payloads to the main jar for non-publish builds.

Published main artifacts do not bundle platform-native or web payloads. Those payloads are published as separate artifacts with explicit artifact IDs; target-neutral resources such as the TeaVM C CMake hook remain in the corresponding main artifact.

Desktop JNI uses `runtime-desktop-jni` as the desktop entry artifact. That artifact depends on `runtime-jni`; `runtime-desktop-jni_<platform>` native-only jars provide desktop native payloads for published builds.

TeaVM C is stricter:

- `runtime-core` and each binding's `core` artifact remain the public API.
- `runtime-c` contains `gen.c.*` target implementations and portable build resources, but no platform native archive.
- `runtime-desktop-c_<platform>` artifacts contain native payloads at the portable resource path plus `META-INF/gdx-teavm.properties`; they contain no Java classes. The Windows artifact includes `windows_x64/mt/{static,shared}` and `windows_x64/md/{static,shared}` payloads, plus the legacy direct MT path for compatibility.
- Native payloads are not bundled into the main `runtime-c` jar.
- Android does not consume desktop native artifacts; it uses `jParser/runtime/android/runtime-android-c`.

Android JNI keeps local project dependencies convenient by adding all ABI payloads to the main `runtime-android` AAR for non-publish builds. Published `runtime-android` is a classes-only Android entry artifact that depends on `runtime-jni`, and `runtime-android_<abi>` artifacts contain one ABI payload each.

Android TeaVM C uses `runtime-android-c` as the Android entry artifact. That artifact depends on `runtime-c`; `runtime-android-c_<abi>` native-only AARs provide Android native payloads.

## TeaVM C Resource And Static Archive Layout

Generation writes portable resources to the C module before jar packaging:

```text
<c-module>/build/generated/jparser/resources/main/
  META-INF/gdx-teavm.properties
  external_cpp/cmake/post_target/jparser_<library>_teavm_c.cmake
  external_cpp/jparser/<library>/glue/**
  external_cpp/jparser/<library>/imports/teavmc_imports.h
  external_cpp/jparser/<library>/custom/**
  external_cpp/jparser/<library>/runtime/RuntimeHelper.h
  external_cpp/jparser/<library>/source/**
```

The main C jar packages this directory. A desktop native split jar supplies the static archive in the same resource namespace:

| Platform | Static archive path inside the native jar |
|----------|-------------------------------------------|
| Windows x64 | `external_cpp/jparser/<library>/native/windows_x64/<Library>64_.lib` |
| Linux x64 | `external_cpp/jparser/<library>/native/linux_x64/lib<Library>64_.a` |
| macOS x64 | `external_cpp/jparser/<library>/native/mac_x64/lib<Library>64_.a` |
| macOS ARM64 | `external_cpp/jparser/<library>/native/mac_arm64/lib<Library>64_.a` |

For the runtime artifacts, `<library>` is `runtime` and `<Library>` is `runtime`. Every native resource jar also contains `META-INF/gdx-teavm.properties` with `ignore-resources=META-INF`, which opts it into gdx-teavm discovery without copying jar metadata into application assets.

The generated CMake hook accepts portable integration variables:

```cmake
set(JPARSER_TEAVMC_APP_TARGET my_teavm_application)
set(JPARSER_TEAVMC_GENERATED_SOURCE_ROOT "${CMAKE_CURRENT_SOURCE_DIR}/generated-c") # optional
set(CMAKE_MSVC_RUNTIME_LIBRARY "MultiThreadedDLL") # optional standard CMake setting (MD)
# These per-library overrides are optional for custom extraction/platform layouts:
set(JPARSER_RUNTIME_TEAVMC_ROOT "/path/to/external_cpp/jparser/runtime")
set(JPARSER_RUNTIME_TEAVMC_LIBRARY "/path/to/libruntime.a")
include(path/to/external_cpp/cmake/post_target/jparser_runtime_teavm_c.cmake)
```

`JPARSER_TEAVMC_APP_TARGET` identifies the target that receives generated glue and static link inputs. `JPARSER_TEAVMC_GENERATED_SOURCE_ROOT` identifies the TeaVM-generated C tree and defaults to `${CMAKE_CURRENT_SOURCE_DIR}/c/src`. Each rendered hook also accepts `JPARSER_<LIBRARY>_TEAVMC_ROOT` and `JPARSER_<LIBRARY>_TEAVMC_LIBRARY`; these allow relocated resources and explicit archives on platforms outside the automatic desktop candidate set. The hook requests `cxx_std_17` without downgrading newer C++ targets and never selects a Windows CRT. Consumers configure the standard target or `CMAKE_MSVC_RUNTIME_LIBRARY` setting themselves. The hook only inspects that existing setting to select the corresponding packaged payload; if neither is set, it selects the MD payload that matches CMake's default MSVC runtime family without changing the target. gdx-teavm discovers and includes the same hook automatically; its `TEAVM_APP_TARGET` variable is supported as a compatibility fallback, so direct CMake consumers do not depend on gdx-teavm.

## Artifact Examples

JNI:

```kotlin
api("com.github.xpenatan.jParser:runtime-desktop-jni:${LibExt.jParserVersion}")
api("com.github.xpenatan.jParser:runtime-desktop-jni_windows_x64:${LibExt.jParserVersion}")
api("com.github.xpenatan.jParser:runtime-desktop-jni_linux_x64:${LibExt.jParserVersion}")
api("com.github.xpenatan.jParser:runtime-desktop-jni_mac_x64:${LibExt.jParserVersion}")
api("com.github.xpenatan.jParser:runtime-desktop-jni_mac_arm64:${LibExt.jParserVersion}")
```

`runtime-desktop-jni` pulls `runtime-jni` transitively.

TeaVM C:

```kotlin
api("com.github.xpenatan.jParser:runtime-c:${LibExt.jParserVersion}")
api("com.github.xpenatan.jParser:runtime-desktop-c_windows_x64:${LibExt.jParserVersion}") // select the host platform
```

Use exactly one matching `runtime-desktop-c_<platform>` artifact. Replace `windows_x64` with `linux_x64`, `mac_x64`, or `mac_arm64` as appropriate.

Web:

```kotlin
implementation("com.github.xpenatan.jParser:runtime-web:${LibExt.jParserVersion}")
implementation("com.github.xpenatan.jParser:runtime-web_wasm:${LibExt.jParserVersion}")
```

Android JNI:

```kotlin
implementation("com.github.xpenatan.jParser:runtime-android:${LibExt.jParserVersion}")
implementation("com.github.xpenatan.jParser:runtime-android_x86:${LibExt.jParserVersion}")
implementation("com.github.xpenatan.jParser:runtime-android_x86_64:${LibExt.jParserVersion}")
implementation("com.github.xpenatan.jParser:runtime-android_armeabi_v7a:${LibExt.jParserVersion}")
implementation("com.github.xpenatan.jParser:runtime-android_arm64_v8a:${LibExt.jParserVersion}")
```

`runtime-android` pulls `runtime-jni` transitively.

Android TeaVM C:

```kotlin
implementation("com.github.xpenatan.jParser:runtime-android-c:${LibExt.jParserVersion}")
implementation("com.github.xpenatan.jParser:runtime-android-c_x86:${LibExt.jParserVersion}")
implementation("com.github.xpenatan.jParser:runtime-android-c_x86_64:${LibExt.jParserVersion}")
implementation("com.github.xpenatan.jParser:runtime-android-c_armeabi_v7a:${LibExt.jParserVersion}")
implementation("com.github.xpenatan.jParser:runtime-android-c_arm64_v8a:${LibExt.jParserVersion}")
```

## Copy Checklist For External Libraries

1. Keep public Java API classes in the binding's `core` artifact.
2. Generate TeaVM C implementations under `gen.c.*`, and make the C artifact expose the public core plus `runtime-c`.
3. Add `build/generated/jparser/resources/main` to the C artifact's main resources and order resource processing after generation.
4. Publish desktop static archives as separate artifact IDs using underscore platform suffixes and the `external_cpp/jparser/<library>/native/<platform>` jar layout.
5. Keep native payload jars/AARs free of Java classes and include the gdx-teavm marker only as a compatibility discovery adapter.
6. For a direct CMake consumer, set `JPARSER_TEAVMC_APP_TARGET`, optionally override the generated-source/resource/archive paths, and include the generated post-target hook.
7. Verify with a clean aggregate generation, C jar build, native resource jar build, and jar-content inspection when changing the layout.
