# Runtime Maven Artifact Strategy

This document explains the Maven artifact setup used by jParser runtime modules so external libraries can replicate it exactly.

Modules covered:
- `idl/runtime/runtime-jni`
- `idl/runtime/runtime-ffm`
- `idl/runtime/runtime-android`
- `idl/runtime/runtime-teavm-web`

## Why this setup exists

The runtime modules support two different use cases:

1. Local/in-repo project dependencies
- Keep main runtime artifacts convenient for module-to-module usage.
- For desktop/web, non-publish builds include native payload in the main jar.

2. Published Maven artifacts
- Keep the main artifact classes-only.
- Publish native/web payload in classifier artifacts.

## Local vs publish behavior

Desktop (`runtime-jni`, `runtime-ffm`) and web (`runtime-teavm-web`) use this gate:

```kotlin
val isPublishingTask = gradle.startParameter.taskNames.any { it.contains("publish", ignoreCase = true) }
```

When `isPublishingTask` is `false`, main jar includes native/web files.
When `isPublishingTask` is `true`, main jar is classes-only and payload is published in classifier artifacts.

Android (`runtime-android`) uses the same publish gate for JNI copy staging so publish tasks do not include `.so` files in a main AAR publication path.

## Classifier artifacts by module

- `runtime-jni`
  - bundled: `desktop`
  - per-platform: `windows_64`, `linux_x64`, `mac_x64`, `mac_arm64`

- `runtime-ffm`
  - bundled: `desktop`
  - per-platform: `windows_64`, `linux_x64`, `mac_x64`, `mac_arm64`

- `runtime-android` (artifactId currently set to `runtime-jni`)
  - bundled: `android`
  - per-ABI: `android_arm64_v8a`, `android_armeabi_v7a`, `android_x86`, `android_x86_64`

- `runtime-teavm-web` (artifactId `runtime-web`)
  - bundled web payload: `wasm` (contains both `idl.js` and `idl.wasm`)

## Reference pattern for desktop modules

```kotlin
val nativeJars = platforms.map { (classifier, config) ->
    tasks.register<Jar>("nativeJar${classifier}") {
        config()
        archiveClassifier.set(classifier)
    }
}

val nativeDesktopJar = tasks.register<Jar>("nativeJarDesktop") {
    archiveClassifier.set("desktop")
    listOf(
        "windows_64" to windowsFile,
        "linux_x64" to linuxFile,
        "mac_x64" to macFile,
        "mac_arm64" to macArmFile,
    ).forEach { (folder, path) ->
        val nativeFile = file(path)
        if(nativeFile.exists()) {
            from(nativeFile) { into(folder) }
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            artifact(nativeDesktopJar)
            nativeJars.forEach { artifact(it) }
        }
    }
}
```

## Reference pattern for Android module

```kotlin
val androidAbiJars = androidAbiFiles.mapNotNull { (classifier, filePath) ->
    if(file(filePath).exists()) {
        tasks.register<Jar>("nativeJar${classifier}") {
            from(filePath)
            archiveClassifier.set(classifier)
        }
    }
    else null
}

val androidAllAbiJar = tasks.register<Jar>("nativeJarAndroid") {
    archiveClassifier.set("android")
    androidAbiFiles.values.forEach { filePath ->
        val nativeFile = file(filePath)
        if(nativeFile.exists()) {
            from(nativeFile) { into(nativeFile.parentFile.name) }
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifact(androidAllAbiJar)
            androidAbiJars.forEach { artifact(it) }
        }
    }
}
```

## Reference pattern for TeaVM web module

```kotlin
val wasmJar = tasks.register<Jar>("wasmJar") {
    from(emscriptenJS, emscriptenWASM)
    archiveClassifier.set("wasm")
}

tasks.named<Jar>("jar") {
    if(!isPublishingTask) {
        from(emscriptenJS, emscriptenWASM)
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            artifact(wasmJar)
        }
    }
}
```

## Copy checklist for external libraries

1. Define platform/ABI native file paths.
2. Create per-platform/per-ABI classifier jar tasks.
3. Create one bundled classifier artifact (`desktop`, `android`, `wasm`) if needed.
4. Use publish-task detection for local vs publish behavior.
5. Publish main classes artifact with `from(components["java"])` when applicable.
6. Attach classifier artifacts in `publishing`.
7. Verify with:
   - local build task (non-publish)
   - `publishMavenPublicationToMavenLocal`
   - jar/aar content inspection for expected payload.

