# Agent Commands Reference

All commands run from repository root.

## Shell-Agnostic Usage

Use `./gradlew` on Linux/macOS and `gradlew.bat` on Windows.

### Runtime (helper)

```text
./gradlew :jParser:runtime:runtime-build:runtime_helper_build_project_windows64_jni
./gradlew :jParser:runtime:runtime-build:runtime_helper_build_project_linux64_jni
./gradlew :jParser:runtime:runtime-build:runtime_helper_build_project_mac64_jni
./gradlew :jParser:runtime:runtime-build:runtime_helper_build_project_macArm_jni
./gradlew :jParser:runtime:runtime-build:runtime_helper_build_project_android_jni
./gradlew :jParser:runtime:runtime-build:runtime_helper_build_project_ios_jni
./gradlew :jParser:runtime:runtime-build:runtime_helper_build_project_windows64_ffm
./gradlew :jParser:runtime:runtime-build:runtime_helper_build_project_linux64_ffm
./gradlew :jParser:runtime:runtime-build:runtime_helper_build_project_mac64_ffm
./gradlew :jParser:runtime:runtime-build:runtime_helper_build_project_macArm_ffm
./gradlew :jParser:runtime:runtime-build:runtime_helper_build_project_windows64_teavm_c
./gradlew :jParser:runtime:runtime-build:runtime_helper_build_project_linux64_teavm_c
./gradlew :jParser:runtime:runtime-build:runtime_helper_build_project_mac64_teavm_c
./gradlew :jParser:runtime:runtime-build:runtime_helper_build_project_macArm_teavm_c
./gradlew :jParser:runtime:runtime-build:runtime_helper_build_project_android_teavm_c
./gradlew :jParser:runtime:runtime-build:runtime_helper_build_project_ios_teavm_c
./gradlew :jParser:runtime:runtime-build:runtime_helper_build_project_web_wasm
```

### TestLib native generation

```text
./gradlew :examples:TestLib:lib:lib-build:TestLib_build_project_windows64_jni
./gradlew :examples:TestLib:lib:lib-build:TestLib_build_project_linux64_jni
./gradlew :examples:TestLib:lib:lib-build:TestLib_build_project_mac64_jni
./gradlew :examples:TestLib:lib:lib-build:TestLib_build_project_macArm_jni
./gradlew :examples:TestLib:lib:lib-build:TestLib_build_project_android_jni
./gradlew :examples:TestLib:lib:lib-build:TestLib_build_project_ios_jni
./gradlew :examples:TestLib:lib:lib-build:TestLib_build_project_windows64_ffm
./gradlew :examples:TestLib:lib:lib-build:TestLib_build_project_linux64_ffm
./gradlew :examples:TestLib:lib:lib-build:TestLib_build_project_mac64_ffm
./gradlew :examples:TestLib:lib:lib-build:TestLib_build_project_macArm_ffm
./gradlew :examples:TestLib:lib:lib-build:TestLib_build_project_windows64_teavm_c
./gradlew :examples:TestLib:lib:lib-build:TestLib_build_project_linux64_teavm_c
./gradlew :examples:TestLib:lib:lib-build:TestLib_build_project_mac64_teavm_c
./gradlew :examples:TestLib:lib:lib-build:TestLib_build_project_macArm_teavm_c
./gradlew :examples:TestLib:lib:lib-build:TestLib_build_project_android_teavm_c
./gradlew :examples:TestLib:lib:lib-build:TestLib_build_project_ios_teavm_c
./gradlew :examples:TestLib:lib:lib-build:TestLib_build_project_web_wasm
```

### TestLib app run/build

```text
./gradlew :examples:TestLib:app:platforms:desktop-jni:TestLib_run_app_desktop_jni
./gradlew :examples:TestLib:app:platforms:desktop-ffm:TestLib_run_app_desktop_ffm
./gradlew :examples:TestLib:app:platforms:desktop-c:TestLib_build_app_desktop_c
./gradlew :examples:TestLib:app:platforms:web:TestLib_run_app_web
./gradlew :examples:TestLib:app:platforms:android:assembleDebug
./gradlew :examples:TestLib:app:platforms:android-c:TestLib_build_app_android_c
```

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
./gradlew :examples:SharedLib:libA:lib-build:LibA_build_project_windows64_jni
./gradlew :examples:SharedLib:libA:lib-build:LibA_build_project_windows64_ffm
./gradlew :examples:SharedLib:libA:lib-build:LibA_build_project_windows64_teavm_c
./gradlew :examples:SharedLib:libA:lib-build:LibA_build_project_android_teavm_c
./gradlew :examples:SharedLib:libA:lib-build:LibA_build_project_web_wasm

./gradlew :examples:SharedLib:libB:lib-build:LibB_build_project_windows64_jni
./gradlew :examples:SharedLib:libB:lib-build:LibB_build_project_windows64_ffm
./gradlew :examples:SharedLib:libB:lib-build:LibB_build_project_windows64_teavm_c
./gradlew :examples:SharedLib:libB:lib-build:LibB_build_project_android_teavm_c
./gradlew :examples:SharedLib:libB:lib-build:LibB_build_project_web_wasm

./gradlew :examples:SharedLib:app:platforms:desktop-jni:SharedLib_run_app_desktop_jni
./gradlew :examples:SharedLib:app:platforms:desktop-ffm:SharedLib_run_app_desktop_ffm
./gradlew :examples:SharedLib:app:platforms:desktop-c:SharedLib_build_app_desktop_c
./gradlew :examples:SharedLib:app:platforms:web:SharedLib_run_app_web
./gradlew :examples:SharedLib:app:platforms:android:assembleDebug
./gradlew :examples:SharedLib:app:platforms:android-c:SharedLib_build_app_android_c
```

## Cross-platform variants

Where applicable, replace `windows64` with `linux64`, `mac64`, or `macArm`.

## Quick compile sanity checks

```text
./gradlew :jParser:gen:gen-core:compileJava
./gradlew :jParser:gen:gen-build-tool:compileJava
```
