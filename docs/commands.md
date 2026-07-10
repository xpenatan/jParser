# Agent Commands Reference

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
./gradlew :examples:TestLib:lib:builder:TestLib_build_project_linux64_teavm_c
./gradlew :examples:TestLib:lib:builder:TestLib_build_project_mac64_teavm_c
./gradlew :examples:TestLib:lib:builder:TestLib_build_project_macArm_teavm_c
./gradlew :examples:TestLib:lib:builder:TestLib_build_project_android_teavm_c
./gradlew :examples:TestLib:lib:builder:TestLib_build_project_ios_teavm_c
./gradlew :examples:TestLib:lib:builder:TestLib_build_project_web_wasm
```

The aggregate `runtime_helper_build_project` and `TestLib_build_project` tasks generate core, JNI, FFM, TeaVM web, and TeaVM C outputs. Platform-specific `*_teavm_c` tasks also compile that platform's native output; use them when producing a static native resource jar.

### TeaVM C jar packaging checks

```text
./gradlew :jParser:runtime:shared:runtime-c:jar
./gradlew :jParser:runtime:desktop:runtime-desktop-c:nativeJar_windows_x64
./gradlew :examples:TestLib:lib:shared:TestLib-c:jar
./gradlew :examples:TestLib:lib:desktop:TestLib-desktop-c:nativeJar_windows_x64
```

Replace `windows_x64` with `linux_x64`, `mac_x64`, or `mac_arm64` after building the corresponding native TeaVM C target. Inspect the main C jar for `gen/c/**` and `external_cpp/cmake/post_target/**`; inspect the native jar for `external_cpp/jparser/<library>/native/<platform>/**` and `META-INF/gdx-teavm.properties`.

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
./gradlew :examples:SharedLib:libA:builder:LibA_build_project_windows64_jni
./gradlew :examples:SharedLib:libA:builder:LibA_build_project_windows64_ffm
./gradlew :examples:SharedLib:libA:builder:LibA_build_project_windows64_teavm_c
./gradlew :examples:SharedLib:libA:builder:LibA_build_project_android_teavm_c
./gradlew :examples:SharedLib:libA:builder:LibA_build_project_web_wasm

./gradlew :examples:SharedLib:libB:builder:LibB_build_project_windows64_jni
./gradlew :examples:SharedLib:libB:builder:LibB_build_project_windows64_ffm
./gradlew :examples:SharedLib:libB:builder:LibB_build_project_windows64_teavm_c
./gradlew :examples:SharedLib:libB:builder:LibB_build_project_android_teavm_c
./gradlew :examples:SharedLib:libB:builder:LibB_build_project_web_wasm

./gradlew :examples:SharedLib:app:platforms:desktop-jni:SharedLib_run_app_desktop_jni
./gradlew :examples:SharedLib:app:platforms:desktop-ffm:SharedLib_run_app_desktop_ffm
./gradlew :examples:SharedLib:app:platforms:desktop-c:SharedLib_build_app_desktop_c
./gradlew :examples:SharedLib:app:platforms:web:SharedLib_run_app_web
./gradlew :examples:SharedLib:app:platforms:android:assembleDebug
./gradlew :examples:SharedLib:app:platforms:android-c:SharedLib_build_app_android_c
```

## Cross-platform variants

Where applicable, replace `windows64` with `linux64`, `mac64`, or `macArm`.

## jParser Gradle plugin

```text
./gradlew -p jParser/tools/gradle-plugin check
./gradlew -p jParser/tools/gradle-plugin validatePlugins

./gradlew :jParser:runtime:plugin:tasks --group jParser --all
./gradlew :jParser:runtime:plugin:jParser_generate
./gradlew :jParser:runtime:plugin:jParser_build_windows64_jni
./gradlew :jParser:runtime:plugin:jParser_build_windows64_ffm
./gradlew :jParser:runtime:plugin:jParser_build_android_jni
./gradlew :jParser:runtime:plugin:jParser_build_web_wasm

./gradlew :examples:TestLib:lib:plugin:tasks --group jParser --all
./gradlew :examples:TestLib:lib:plugin:jParser_generate
./gradlew :examples:TestLib:lib:plugin:jParser_build_windows64_jni
./gradlew :examples:TestLib:lib:plugin:jParser_build_windows64_ffm
./gradlew :examples:TestLib:lib:plugin:jParser_build_android_jni
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

The plugin id is `com.github.xpenatan.jparser`; the Maven implementation artifact is `com.github.xpenatan.jParser:jparser-gradle-plugin`.

## Quick compile sanity checks

```text
./gradlew :jParser:gen:gen-core:compileJava
./gradlew :jParser:gen:gen-build-tool:compileJava
./gradlew -p jParser/tools/gradle-plugin compileKotlin
```
