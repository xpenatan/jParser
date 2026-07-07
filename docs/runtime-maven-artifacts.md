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

The main `runtime-c` artifact is published from `jParser/runtime/shared/runtime-c` and contains generated Java classes only. Desktop native split artifacts are published from `jParser/runtime/desktop/runtime-desktop-c` and contain only the compiled native payload for that platform. Android TeaVM C publishes a main `runtime-android-c` AAR from `jParser/runtime/android/runtime-android-c` with a dependency on `runtime-c`, plus native split AARs for each ABI.

`jParser/runtime/android/runtime-android-c` builds local native-only AAR files with the `runtime-android-c-<abi>` archive base name. Maven publication uses the public artifact IDs `runtime-android-c_<abi>`.

## Local vs Publish Behavior

Desktop JNI (`runtime-desktop-jni`), desktop FFM, and web (`runtime-web`) keep local project dependencies convenient by adding native/web payloads to the main jar for non-publish builds.

Published main artifacts are classes-only. Native/web payloads are published as separate artifacts with explicit artifact IDs.

Desktop JNI uses `runtime-desktop-jni` as the desktop entry artifact. That artifact depends on `runtime-jni`; `runtime-desktop-jni_<platform>` native-only jars provide desktop native payloads for published builds.

TeaVM C is stricter:

- `runtime-c` is always Java classes only.
- `runtime-desktop-c_<platform>` artifacts are native payload only.
- Native payloads are not bundled into the main `runtime-c` jar.
- Android does not consume desktop native artifacts; it uses `jParser/runtime/android/runtime-android-c`.

Android JNI keeps local project dependencies convenient by adding all ABI payloads to the main `runtime-android` AAR for non-publish builds. Published `runtime-android` is a classes-only Android entry artifact that depends on `runtime-jni`, and `runtime-android_<abi>` artifacts contain one ABI payload each.

Android TeaVM C uses `runtime-android-c` as the Android entry artifact. That artifact depends on `runtime-c`; `runtime-android-c_<abi>` native-only AARs provide Android native payloads.

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
api("com.github.xpenatan.jParser:runtime-desktop-c_windows_x64:${LibExt.jParserVersion}")
api("com.github.xpenatan.jParser:runtime-desktop-c_linux_x64:${LibExt.jParserVersion}")
api("com.github.xpenatan.jParser:runtime-desktop-c_mac_x64:${LibExt.jParserVersion}")
api("com.github.xpenatan.jParser:runtime-desktop-c_mac_arm64:${LibExt.jParserVersion}")
```

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

1. Keep generated/public Java classes in the main runtime artifact.
2. Publish platform native payloads as separate artifact IDs using underscore platform suffixes.
3. Keep native payload jars/AARs free of generated Java classes.
4. Keep main publish artifacts classes-only.
5. Verify with a local compile/generation task and jar content inspection when changing artifact layout.
