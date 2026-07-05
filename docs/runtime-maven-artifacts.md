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

## Published Artifact Names

Existing JVM/web artifacts keep their names:

- `runtime-core`
- `runtime-jni`
- `runtime-ffm`
- `runtime-web`
- `runtime-android`

TeaVM C artifacts use separate modules for generated Java and platform native payloads:

- `runtime-c`
- `runtime-c_windows_x64`
- `runtime-c_linux_x64`
- `runtime-c_mac_x64`
- `runtime-c_mac_arm64`
- `runtime-c_android`

The main `runtime-c` artifact is published from `jParser/runtime/shared/c` and contains generated Java classes only. Desktop native split artifacts are published from `jParser/runtime/desktop/c` and contain only the compiled native payload for that platform. Android payloads are published from `jParser/runtime/android/c`.

## Local vs Publish Behavior

Desktop JVM modules (`runtime-jni`, `runtime-ffm`) and web (`runtime-web`) keep local project dependencies convenient by adding native/web payloads to the main jar for non-publish builds.

Published main artifacts are classes-only. Native/web payloads are published as separate artifacts with explicit artifact IDs.

TeaVM C is stricter:

- `runtime-c` is always Java classes only.
- `runtime-c_<platform>` artifacts are native payload only.
- Native payloads are not bundled into the main `runtime-c` jar.
- Android does not consume desktop native artifacts; it uses `jParser/runtime/android/c`.

Android (`runtime-android`) publishes a single AAR containing supported ABI `.so` files.

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
api("com.github.xpenatan.jParser:runtime-c_android:${LibExt.jParserVersion}")
```

Web:

```kotlin
implementation("com.github.xpenatan.jParser:runtime-web:${LibExt.jParserVersion}")
implementation("com.github.xpenatan.jParser:runtime-web_wasm:${LibExt.jParserVersion}")
```

Android:

```kotlin
implementation("com.github.xpenatan.jParser:runtime-android:${LibExt.jParserVersion}")
```

## Copy Checklist For External Libraries

1. Keep generated/public Java classes in the main runtime artifact.
2. Publish platform native payloads as separate artifact IDs using underscore platform suffixes.
3. Keep native payload jars free of generated Java classes.
4. Keep main publish artifacts classes-only.
5. Verify with a local compile/generation task and jar content inspection when changing artifact layout.
