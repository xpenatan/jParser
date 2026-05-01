# Agent Commands Reference

All commands run from repository root.

## Shell-Agnostic Usage

Use `./gradlew` on Linux/macOS and `gradlew.bat` on Windows.

### Runtime (IDL helper)

```text
./gradlew :idl:runtime:runtime-build:idl_helper_build_project_windows64_jni
./gradlew :idl:runtime:runtime-build:idl_helper_build_project_linux64_jni
./gradlew :idl:runtime:runtime-build:idl_helper_build_project_mac64_jni
./gradlew :idl:runtime:runtime-build:idl_helper_build_project_macArm_jni
./gradlew :idl:runtime:runtime-build:idl_helper_build_project_android_jni
./gradlew :idl:runtime:runtime-build:idl_helper_build_project_ios_jni
./gradlew :idl:runtime:runtime-build:idl_helper_build_project_windows64_ffm
./gradlew :idl:runtime:runtime-build:idl_helper_build_project_linux64_ffm
./gradlew :idl:runtime:runtime-build:idl_helper_build_project_mac64_ffm
./gradlew :idl:runtime:runtime-build:idl_helper_build_project_macArm_ffm
./gradlew :idl:runtime:runtime-build:idl_helper_build_project_web_wasm
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
./gradlew :examples:TestLib:lib:lib-build:TestLib_build_project_web_wasm
```

### TestLib app run/build

```text
./gradlew :examples:TestLib:app:desktop-jni:TestLib_run_app_desktop_jni
./gradlew :examples:TestLib:app:desktop-ffm:TestLib_run_app_desktop_ffm
./gradlew :examples:TestLib:app:web:TestLib_run_app_web
./gradlew :examples:TestLib:app:android:assembleDebug
```

### TestLib app tests

```text
./gradlew :examples:TestLib:app:desktop-jni:test
./gradlew :examples:TestLib:app:desktop-ffm:test
```

### Benchmarks

```text
./gradlew :examples:TestLib:app:desktop-jni:TestLib_throughput_benchmark_jni
./gradlew :examples:TestLib:app:desktop-ffm:TestLib_throughput_benchmark_ffm
./gradlew :examples:TestLib:app:desktop-ffm:TestLib_throughput_benchmark_compare

./gradlew :examples:TestLib:app:desktop-jni:TestLib_fps_benchmark_jni
./gradlew :examples:TestLib:app:desktop-ffm:TestLib_fps_benchmark_ffm
./gradlew :examples:TestLib:app:desktop-ffm:TestLib_fps_benchmark_compare

./gradlew :examples:TestLib:app:desktop-jni:TestLib_enum_benchmark_jni
./gradlew :examples:TestLib:app:desktop-ffm:TestLib_enum_benchmark_ffm
./gradlew :examples:TestLib:app:desktop-ffm:TestLib_enum_benchmark_compare

./gradlew :examples:TestLib:app:web:TestLib_run_benchmark_web
```

## SharedLib

Build libA before libB.

```text
./gradlew :examples:SharedLib:libA:lib-build:LibA_build_project_windows64_jni
./gradlew :examples:SharedLib:libA:lib-build:LibA_build_project_windows64_ffm
./gradlew :examples:SharedLib:libA:lib-build:LibA_build_project_web_wasm

./gradlew :examples:SharedLib:libB:lib-build:LibB_build_project_windows64_jni
./gradlew :examples:SharedLib:libB:lib-build:LibB_build_project_windows64_ffm
./gradlew :examples:SharedLib:libB:lib-build:LibB_build_project_web_wasm

./gradlew :examples:SharedLib:app:desktop-jni:SharedLib_run_app_desktop_jni
./gradlew :examples:SharedLib:app:desktop-ffm:SharedLib_run_app_desktop_ffm
./gradlew :examples:SharedLib:app:web:SharedLib_run_app_web
./gradlew :examples:SharedLib:app:android:assembleDebug
```

## Cross-platform variants

Where applicable, replace `windows64` with `linux64`, `mac64`, or `macArm`.

## Quick compile sanity checks

```text
./gradlew :jParser:jParser-core:compileJava
./gradlew :jParser:jParser-build-tool:compileJava
```
