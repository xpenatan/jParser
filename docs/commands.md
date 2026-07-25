# Command Reference

All commands run from repository root.

## Shell-Agnostic Usage

Use `./gradlew` on Linux/macOS and `gradlew.bat` on Windows.

### Runtime (helper)

```text
./gradlew :jParser:runtime:builder:runtime_helper_build_project
./gradlew :jParser:runtime:builder:runtime_helper_build_project_windows64_jni
./gradlew :jParser:runtime:builder:runtime_helper_build_project_linux64_jni
./gradlew :jParser:runtime:builder:runtime_helper_build_project_mac64_jni
./gradlew :jParser:runtime:builder:runtime_helper_build_project_macArm_jni
./gradlew :jParser:runtime:builder:runtime_helper_build_project_android_jni
./gradlew :jParser:runtime:builder:runtime_helper_build_project_ios_jni
./gradlew :jParser:runtime:builder:runtime_helper_build_project_windows64_ffm
./gradlew :jParser:runtime:builder:runtime_helper_build_project_linux64_ffm
./gradlew :jParser:runtime:builder:runtime_helper_build_project_mac64_ffm
./gradlew :jParser:runtime:builder:runtime_helper_build_project_macArm_ffm
./gradlew :jParser:runtime:builder:runtime_helper_build_project_windows64_teavm_c
./gradlew :jParser:runtime:builder:runtime_helper_build_project_windows64_teavm_c_mt
./gradlew :jParser:runtime:builder:runtime_helper_build_project_windows64_teavm_c_md
./gradlew :jParser:runtime:builder:runtime_helper_build_project_linux64_teavm_c
./gradlew :jParser:runtime:builder:runtime_helper_build_project_mac64_teavm_c
./gradlew :jParser:runtime:builder:runtime_helper_build_project_macArm_teavm_c
./gradlew :jParser:runtime:builder:runtime_helper_build_project_android_teavm_c
./gradlew :jParser:runtime:builder:runtime_helper_build_project_ios_teavm_c
./gradlew :jParser:runtime:builder:runtime_helper_build_project_web_wasm
```

### TestLib native generation

```text
./gradlew :examples:TestLib:lib:builder:TestLib_build_project
./gradlew :examples:TestLib:lib:builder:TestLib_build_project_windows64_jni
./gradlew :examples:TestLib:lib:builder:TestLib_build_project_linux64_jni
./gradlew :examples:TestLib:lib:builder:TestLib_build_project_mac64_jni
./gradlew :examples:TestLib:lib:builder:TestLib_build_project_macArm_jni
./gradlew :examples:TestLib:lib:builder:TestLib_build_project_android_jni
./gradlew :examples:TestLib:lib:builder:TestLib_build_project_ios_jni
./gradlew :examples:TestLib:lib:builder:TestLib_build_project_windows64_ffm
./gradlew :examples:TestLib:lib:builder:TestLib_build_project_linux64_ffm
./gradlew :examples:TestLib:lib:builder:TestLib_build_project_mac64_ffm
./gradlew :examples:TestLib:lib:builder:TestLib_build_project_macArm_ffm
./gradlew :examples:TestLib:lib:builder:TestLib_build_project_windows64_teavm_c
./gradlew :examples:TestLib:lib:plugin:jParser_build_windows64_teavm_c_mt
./gradlew :examples:TestLib:lib:plugin:jParser_build_windows64_teavm_c_md
./gradlew :examples:TestLib:lib:builder:TestLib_build_project_linux64_teavm_c
./gradlew :examples:TestLib:lib:builder:TestLib_build_project_mac64_teavm_c
./gradlew :examples:TestLib:lib:builder:TestLib_build_project_macArm_teavm_c
./gradlew :examples:TestLib:lib:builder:TestLib_build_project_android_teavm_c
./gradlew :examples:TestLib:lib:builder:TestLib_build_project_ios_teavm_c
./gradlew :examples:TestLib:lib:builder:TestLib_build_project_web_wasm
```

The aggregate `runtime_helper_build_project` and `TestLib_build_project` tasks generate core, JNI, FFM, TeaVM web, and TeaVM C outputs. Platform-specific `*_teavm_c` tasks also compile that platform's native output; use them when producing a TeaVM C native payload jar. The unsuffixed Windows runtime TeaVM C task builds both MT and MD variants; use its `_mt` or `_md` task when only one CRT family is needed. TestLib's plugin tasks demonstrate the general target-variant API and must both run before packaging its dual-runtime Windows native jar.

The same `*_teavm_c` task names are used for every native linkage mode. Select the mode in the Gradle plugin extension:

```kotlin
import com.github.xpenatan.jParser.gradle.TeaVMCLinkage

jParser {
    teaVMCLinkage.set(TeaVMCLinkage.SHARED_LINKED)
}
```

For a manual `BuilderTool` build, assign `BuildToolOptions.BuildToolParams.teaVMCLinkage` before creating `BuildToolOptions`. Builds launched through `JParserBuildRunner` may instead pass `-Djparser.teaVMCLinkage=RUNTIME_LOADED`. `STATIC` is used when no value is configured.

Windows producer CRT selection is independent from linkage and uses the same generic compiler hooks as every other platform:

```kotlin
import com.github.xpenatan.jParser.gradle.JParserTargets

jParser {
    native {
        target(JParserTargets.WINDOWS64_TEAVM_C) {
            compileFlag("/MD")
        }
    }
}
```

jParser adds no CRT flag when none is supplied. The system-property runner accepts `-Djparser.native.windows64_teavm_c.compileFlags=/MD` when `windows64_teavm_c` is listed in `jparser.native.targets`. The same `compileFlag(...)`/`compileFlags` mechanism accepts GCC, Clang, Android NDK, or Apple toolchain flags for Linux, macOS, Android, and iOS targets. CMake consumers use the standard `CMAKE_MSVC_RUNTIME_LIBRARY` setting; generated hooks preserve it and use CMake's default MD family only for payload lookup when no selection exists.

### TeaVM C jar packaging checks

```text
./gradlew :jParser:loader:loader-c:jar
./gradlew :jParser:runtime:shared:runtime-c:jar
./gradlew :jParser:runtime:desktop:runtime-desktop-c:nativeJar_windows_x64
./gradlew :jParser:runtime:ios:runtime-ios-c:jar
./gradlew :examples:TestLib:lib:shared:TestLib-c:jar
./gradlew :examples:TestLib:lib:desktop:TestLib-desktop-c:nativeJar_windows_x64
./gradlew :examples:TestLib:lib:ios:TestLib-ios-c:jar
```

Replace `windows_x64` with `linux_x64`, `mac_x64`, or `mac_arm64` after building the corresponding desktop TeaVM C target. iOS jar tasks run on macOS and first build device ARM64 plus simulator ARM64/x86_64 slices. Inspect `loader-c` for `external_cpp/jparser/loader/teavmc_loader.{h,cpp}` and its `jparser_00_teavmc_loader.cmake` hook. Inspect the main binding C jar for `gen/c/**`, `external_cpp/cmake/post_target/**`, and `external_cpp/jparser/<library>/teavmcabi/**`; inspect the native jar for `external_cpp/jparser/<library>/native/<platform>/**` and `META-INF/gdx-teavm.properties`. Dynamic linkage payloads must include the matching DLL, SO, or dylib (and a Windows import library for `SHARED_LINKED`). A consumer that discovers these files inside a native resource jar must extract them before CMake can link or stage them and before the operating-system loader can open them. The current gdx-teavm extractor also needs its extension allowlist updated to copy `.dll`, `.so`, and `.dylib` payloads.

### TestLib app run/build

```text
./gradlew :examples:TestLib:app:platforms:desktop-jni:TestLib_run_app_desktop_jni
./gradlew :examples:TestLib:app:platforms:desktop-ffm:TestLib_run_app_desktop_ffm
./gradlew :examples:TestLib:app:platforms:desktop-c:TestLib_build_app_desktop_c
./gradlew :examples:TestLib:app:platforms:web:TestLib_run_app_web
./gradlew :examples:TestLib:app:platforms:android:assembleDebug
./gradlew :examples:TestLib:app:platforms:android-c:TestLib_build_app_android_c
./gradlew :examples:TestLib:app:platforms:ios-c:TestLib_build_app_ios_c
```

The iOS command requires macOS, Xcode, and CMake. The committed TestLib and SharedLib modules are handwritten custom emulator applications: each renders its running/pass/fail state in a real iPhone Simulator while consuming the TeaVM C inputs generated and packaged by jParser. jParser does not generate their launcher, UI, `Info.plist`, Xcode/CMake project, or signing configuration. Override the host-derived simulator architecture with `-PiosSimulatorArch=arm64` or `-PiosSimulatorArch=x86_64`.

### TestLib app tests

```text
./gradlew :examples:TestLib:app:platforms:desktop-jni:test
./gradlew :examples:TestLib:app:platforms:desktop-ffm:test
```

### Benchmarks

```text
./gradlew :examples:TestLib:app:platforms:desktop-jni:TestLib_throughput_benchmark_jni
./gradlew :examples:TestLib:app:platforms:desktop-ffm:TestLib_throughput_benchmark_ffm
./gradlew :examples:TestLib:app:platforms:desktop-ffm:TestLib_throughput_benchmark_compare

./gradlew :examples:TestLib:app:platforms:desktop-jni:TestLib_fps_benchmark_jni
./gradlew :examples:TestLib:app:platforms:desktop-ffm:TestLib_fps_benchmark_ffm
./gradlew :examples:TestLib:app:platforms:desktop-ffm:TestLib_fps_benchmark_compare

./gradlew :examples:TestLib:app:platforms:desktop-jni:TestLib_enum_benchmark_jni
./gradlew :examples:TestLib:app:platforms:desktop-ffm:TestLib_enum_benchmark_ffm
./gradlew :examples:TestLib:app:platforms:desktop-ffm:TestLib_enum_benchmark_compare

./gradlew :examples:TestLib:app:platforms:web:TestLib_run_benchmark_web

./gradlew :jParser:benchmark:benchmark-core:perf_smoke
./gradlew :phase3_perf_smoke
```

## SharedLib

Build libA before libB.

```text
./gradlew :examples:SharedLib:libA:builder:LibA_build_project_windows64_jni
./gradlew :examples:SharedLib:libA:builder:LibA_build_project_windows64_ffm
./gradlew :examples:SharedLib:libA:builder:LibA_build_project_windows64_teavm_c
./gradlew :examples:SharedLib:libA:builder:LibA_build_project_android_teavm_c
./gradlew :examples:SharedLib:libA:builder:LibA_build_project_ios_teavm_c
./gradlew :examples:SharedLib:libA:builder:LibA_build_project_web_wasm

./gradlew :examples:SharedLib:libB:builder:LibB_build_project_windows64_jni
./gradlew :examples:SharedLib:libB:builder:LibB_build_project_windows64_ffm
./gradlew :examples:SharedLib:libB:builder:LibB_build_project_windows64_teavm_c
./gradlew :examples:SharedLib:libB:builder:LibB_build_project_android_teavm_c
./gradlew :examples:SharedLib:libB:builder:LibB_build_project_ios_teavm_c
./gradlew :examples:SharedLib:libB:builder:LibB_build_project_web_wasm

./gradlew :examples:SharedLib:app:platforms:desktop-jni:SharedLib_run_app_desktop_jni
./gradlew :examples:SharedLib:app:platforms:desktop-ffm:SharedLib_run_app_desktop_ffm
./gradlew :examples:SharedLib:app:platforms:desktop-c:SharedLib_build_app_desktop_c
./gradlew :examples:SharedLib:app:platforms:web:SharedLib_run_app_web
./gradlew :examples:SharedLib:app:platforms:android:assembleDebug
./gradlew :examples:SharedLib:app:platforms:android-c:SharedLib_build_app_android_c
./gradlew :examples:SharedLib:app:platforms:ios-c:SharedLib_build_app_ios_c
```

## Cross-platform variants

Where applicable, replace `windows64` with `linux64`, `mac64`, or `macArm`.

## Publishing

jParser uses the Easy Publishing plugin to coordinate the root-owned library
modules and the standalone `gradle-plugin` build.

```text
./gradlew prepareSnapshot
./gradlew publishSnapshot
./gradlew prepareRelease
./gradlew publishRelease
```

`prepareSnapshot` and `prepareRelease` create local Maven repositories in
`build/snapshot-deploy` and `build/staging-deploy`, respectively. They do not
upload artifacts. Both tasks prepare every native platform publication.
Missing native payloads are reported as warnings and omitted from the affected
artifact, allowing local preparation to inspect the available outputs. Build
and collect every cross-platform native payload before publishing a complete
repository.

`publishSnapshot` uploads to the configured Sonatype snapshot repository.
`publishRelease` prepares the release repository, creates the Maven Central
bundle, and uploads it through the Central Portal. Remote publishing reads
`CENTRAL_PORTAL_USERNAME`, `CENTRAL_PORTAL_PASSWORD`, `SIGNING_KEY`, and
`SIGNING_PASSWORD` from the environment. Do not request snapshot and release
publishing tasks in the same Gradle invocation.

## jParser Gradle plugin

```text
./gradlew -p gradle-plugin check
./gradlew -p gradle-plugin validatePlugins

./gradlew :examples:TestLib:lib:plugin:tasks --group jParser --all
./gradlew :examples:TestLib:lib:plugin:jParser_generate
./gradlew :examples:TestLib:lib:plugin:jParser_build_windows64_jni
./gradlew :examples:TestLib:lib:plugin:jParser_build_windows64_ffm
./gradlew :examples:TestLib:lib:plugin:jParser_build_android_jni
./gradlew :examples:TestLib:lib:plugin:jParser_build_ios_teavm_c
./gradlew :examples:TestLib:lib:plugin:jParser_build_web_wasm

./gradlew :examples:SharedLib:libA:plugin:tasks --group jParser --all
./gradlew :examples:SharedLib:libA:plugin:jParser_generate
./gradlew :examples:SharedLib:libB:plugin:tasks --group jParser --all
./gradlew :examples:SharedLib:libB:plugin:jParser_generate
./gradlew :examples:SharedLib:libB:plugin:jParser_build_windows64_jni
./gradlew :examples:SharedLib:libB:plugin:jParser_build_windows64_ffm
./gradlew :examples:SharedLib:libB:plugin:jParser_build_android_jni
./gradlew :examples:SharedLib:libB:plugin:jParser_build_web_wasm
```

The plugin id is `com.github.xpenatan.jParser`; the Maven implementation artifact is `com.github.xpenatan.jParser:jparser-gradle-plugin`.

## Quick compile sanity checks

```text
./gradlew :jParser:gen:gen-core:compileJava
./gradlew :jParser:gen:gen-build-tool:compileJava
./gradlew -p gradle-plugin compileKotlin
```
