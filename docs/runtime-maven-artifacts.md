# Runtime Maven Artifact Strategy

This document explains the Maven artifact setup used by jParser runtime modules so external libraries can replicate it exactly.

## Module Layout

Top-level runtime modules:

- `jParser/runtime/base`
- `jParser/runtime/builder`
- `jParser/runtime/core`
- `jParser/runtime/shared/c`
- `jParser/runtime/desktop/c`
- `jParser/runtime/android/c`

Runtime implementation modules:

- `jParser/runtime/shared/jni`
- `jParser/runtime/desktop/ffm`
- `jParser/runtime/web/wasm`
- `jParser/runtime/android/jni`
- `jParser/runtime/android/c`

## Published Artifact Names

Existing JVM/web artifacts keep their names:

- `runtime-core`
- `runtime-jni`
- `runtime-ffm`
- `runtime-web`
- `runtime-android`

Android JNI native payloads are split by ABI:

- `runtime-android_x86`
- `runtime-android_x86_64`
- `runtime-android_armeabi_v7a`
- `runtime-android_arm64_v8a`

TeaVM C artifacts use separate modules for generated Java and platform native payloads:

- `runtime-c`
- `runtime-c_windows_x64`
- `runtime-c_linux_x64`
- `runtime-c_mac_x64`
- `runtime-c_mac_arm64`
- `runtime-c_android_x86`
- `runtime-c_android_x86_64`
- `runtime-c_android_armeabi_v7a`
- `runtime-c_android_arm64_v8a`

The main `runtime-c` artifact is published from `jParser/runtime/shared/c` and contains generated Java classes only. Desktop native split artifacts are published from `jParser/runtime/desktop/c` and contain only the compiled native payload for that platform. Android TeaVM C native split artifacts are published from `jParser/runtime/android/c`.

`jParser/runtime/android/c` builds local native-only AAR files with the `runtime-android-c-<abi>` archive base name. Maven publication still uses the public artifact IDs `runtime-c_android_<abi>`.

## Local vs Publish Behavior

Desktop JVM modules (`runtime-jni`, `runtime-ffm`) and web (`runtime-web`) keep local project dependencies convenient by adding native/web payloads to the main jar for non-publish builds.

Published main artifacts are classes-only. Native/web payloads are published as separate artifacts with explicit artifact IDs.

TeaVM C is stricter:

- `runtime-c` is always Java classes only.
- `runtime-c_<platform>` artifacts are native payload only.
- Native payloads are not bundled into the main `runtime-c` jar.
- Android does not consume desktop native artifacts; it uses `jParser/runtime/android/c`.

Android JNI keeps local project dependencies convenient by adding all ABI payloads to the main `runtime-android` AAR for non-publish builds. Published `runtime-android` is classes-only, and `runtime-android_<abi>` artifacts contain one ABI payload each.

Android TeaVM C uses `runtime-c` for generated Java classes and `runtime-c_android_<abi>` native-only AARs for Android native payloads.

## Artifact Examples

JNI:

```kotlin
api("com.github.xpenatan.jParser:runtime-jni:${LibExt.jParserVersion}")
api("com.github.xpenatan.jParser:runtime-jni_windows_x64:${LibExt.jParserVersion}")
api("com.github.xpenatan.jParser:runtime-jni_linux_x64:${LibExt.jParserVersion}")
api("com.github.xpenatan.jParser:runtime-jni_mac_x64:${LibExt.jParserVersion}")
api("com.github.xpenatan.jParser:runtime-jni_mac_arm64:${LibExt.jParserVersion}")
```

TeaVM C:

```kotlin
api("com.github.xpenatan.jParser:runtime-c:${LibExt.jParserVersion}")
api("com.github.xpenatan.jParser:runtime-c_windows_x64:${LibExt.jParserVersion}")
api("com.github.xpenatan.jParser:runtime-c_linux_x64:${LibExt.jParserVersion}")
api("com.github.xpenatan.jParser:runtime-c_mac_x64:${LibExt.jParserVersion}")
api("com.github.xpenatan.jParser:runtime-c_mac_arm64:${LibExt.jParserVersion}")
api("com.github.xpenatan.jParser:runtime-c_android_x86:${LibExt.jParserVersion}")
api("com.github.xpenatan.jParser:runtime-c_android_x86_64:${LibExt.jParserVersion}")
api("com.github.xpenatan.jParser:runtime-c_android_armeabi_v7a:${LibExt.jParserVersion}")
api("com.github.xpenatan.jParser:runtime-c_android_arm64_v8a:${LibExt.jParserVersion}")
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

Android TeaVM C:

```kotlin
implementation("com.github.xpenatan.jParser:runtime-c:${LibExt.jParserVersion}")
implementation("com.github.xpenatan.jParser:runtime-c_android_x86:${LibExt.jParserVersion}")
implementation("com.github.xpenatan.jParser:runtime-c_android_x86_64:${LibExt.jParserVersion}")
implementation("com.github.xpenatan.jParser:runtime-c_android_armeabi_v7a:${LibExt.jParserVersion}")
implementation("com.github.xpenatan.jParser:runtime-c_android_arm64_v8a:${LibExt.jParserVersion}")
```

## Copy Checklist For External Libraries

1. Keep generated/public Java classes in the main runtime artifact.
2. Publish platform native payloads as separate artifact IDs using underscore platform suffixes.
3. Keep native payload jars/AARs free of generated Java classes.
4. Keep main publish artifacts classes-only.
5. Verify with a local compile/generation task and jar content inspection when changing artifact layout.
