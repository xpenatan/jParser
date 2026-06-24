# Runtime Maven Artifact Strategy

This document explains the Maven artifact setup used by jParser runtime modules so external libraries can replicate it exactly.

## Module Layout

Top-level runtime modules:

- `jParser/runtime/runtime-base`
- `jParser/runtime/runtime-build`
- `jParser/runtime/runtime-core`
- `jParser/runtime/runtime-c/core`
- `jParser/runtime/runtime-c/desktop`
- `jParser/runtime/runtime-c/android`

JVM/Java-side runtime implementation modules are grouped internally:

- `jParser/runtime/runtime-jvm/jni`
- `jParser/runtime/runtime-jvm/ffm`
- `jParser/runtime/runtime-jvm/web`
- `jParser/runtime/runtime-jvm/android`

The internal `runtime-jvm` folder is not part of the public Maven artifact names.

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

The main `runtime-c` artifact is published from `runtime-c/core` and contains generated Java classes only. Desktop native split artifacts are published from `runtime-c/desktop` and contain only the compiled native payload for that platform. Android payloads are published from their own child module.

## Local vs Publish Behavior

Desktop JVM modules (`runtime-jni`, `runtime-ffm`) and web (`runtime-web`) keep local project dependencies convenient by adding native/web payloads to the main jar for non-publish builds.

Published main artifacts are classes-only. Native/web payloads are published as separate artifacts with explicit artifact IDs.

TeaVM C is stricter:

- `runtime-c` is always Java classes only.
- `runtime-c_<platform>` artifacts are native payload only.
- Native payloads are not bundled into the main `runtime-c` jar.
- Android does not consume desktop native artifacts; it uses `runtime-c/android`.

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
