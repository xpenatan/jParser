# Agent Commands Reference

All commands run from repository root.

## Windows (PowerShell)

Use `gradlew.bat`.

### Runtime (IDL helper)

```powershell
gradlew.bat :idl:runtime:runtime-build:idl_helper_build_project_windows64_jni
gradlew.bat :idl:runtime:runtime-build:idl_helper_build_project_windows64_ffm
```

### TestLib native generation

```powershell
gradlew.bat :examples:TestLib:lib:lib-build:TestLib_build_project_windows64_jni
gradlew.bat :examples:TestLib:lib:lib-build:TestLib_build_project_windows64_ffm
```

### TestLib app run

```powershell
gradlew.bat :examples:TestLib:app:desktop-jni:TestLib_run_app_desktop_jni
gradlew.bat :examples:TestLib:app:desktop-ffm:TestLib_run_app_desktop_ffm
```

### TestLib app tests

```powershell
gradlew.bat :examples:TestLib:app:desktop-jni:test
gradlew.bat :examples:TestLib:app:desktop-ffm:test
```

### Benchmarks

```powershell
gradlew.bat :examples:TestLib:app:desktop-ffm:TestLib_throughput_benchmark_compare
gradlew.bat :examples:TestLib:app:desktop-jni:TestLib_throughput_benchmark_jni
gradlew.bat :examples:TestLib:app:desktop-ffm:TestLib_throughput_benchmark_ffm

gradlew.bat :examples:TestLib:app:desktop-ffm:TestLib_fps_benchmark_compare
gradlew.bat :examples:TestLib:app:desktop-jni:TestLib_fps_benchmark_jni
gradlew.bat :examples:TestLib:app:desktop-ffm:TestLib_fps_benchmark_ffm
```

## SharedLib

Build libA before libB.

```powershell
gradlew.bat :examples:SharedLib:libA:lib-build:LibA_build_project_windows64_jni
gradlew.bat :examples:SharedLib:libA:lib-build:LibA_build_project_windows64_ffm

gradlew.bat :examples:SharedLib:libB:lib-build:LibB_build_project_windows64_jni
gradlew.bat :examples:SharedLib:libB:lib-build:LibB_build_project_windows64_ffm

gradlew.bat :examples:SharedLib:app:desktop-jni:SharedLib_run_app_desktop_jni
gradlew.bat :examples:SharedLib:app:desktop-ffm:SharedLib_run_app_desktop_ffm
```

## Cross-platform variants

Replace `windows64` with `linux64`, `mac64`, or `macArm` where available.

## Quick compile sanity checks

```powershell
gradlew.bat :jParser:jParser-core:compileJava
gradlew.bat :jParser:jParser-build-tool:compileJava
```
# Agent Commands Reference

All commands run from repository root.

## Windows (PowerShell)

Use `gradlew.bat`.

### Runtime (IDL helper)

```powershell
gradlew.bat :idl:runtime:runtime-build:idl_helper_build_project_windows64_jni
gradlew.bat :idl:runtime:runtime-build:idl_helper_build_project_windows64_ffm
```

### TestLib native generation

```powershell
gradlew.bat :examples:TestLib:lib:lib-build:TestLib_build_project_windows64_jni
gradlew.bat :examples:TestLib:lib:lib-build:TestLib_build_project_windows64_ffm
```

### TestLib app run

```powershell
gradlew.bat :examples:TestLib:app:desktop-jni:TestLib_run_app_desktop_jni
gradlew.bat :examples:TestLib:app:desktop-ffm:TestLib_run_app_desktop_ffm
```

### TestLib app tests

```powershell
gradlew.bat :examples:TestLib:app:desktop-jni:test
gradlew.bat :examples:TestLib:app:desktop-ffm:test
```

### Benchmarks

```powershell
gradlew.bat :examples:TestLib:app:desktop-ffm:TestLib_throughput_benchmark_compare
gradlew.bat :examples:TestLib:app:desktop-jni:TestLib_throughput_benchmark_jni
gradlew.bat :examples:TestLib:app:desktop-ffm:TestLib_throughput_benchmark_ffm

gradlew.bat :examples:TestLib:app:desktop-ffm:TestLib_fps_benchmark_compare
gradlew.bat :examples:TestLib:app:desktop-jni:TestLib_fps_benchmark_jni
gradlew.bat :examples:TestLib:app:desktop-ffm:TestLib_fps_benchmark_ffm
```

## SharedLib

Build libA before libB.

```powershell
gradlew.bat :examples:SharedLib:libA:lib-build:LibA_build_project_windows64_jni
gradlew.bat :examples:SharedLib:libA:lib-build:LibA_build_project_windows64_ffm

gradlew.bat :examples:SharedLib:libB:lib-build:LibB_build_project_windows64_jni
gradlew.bat :examples:SharedLib:libB:lib-build:LibB_build_project_windows64_ffm

gradlew.bat :examples:SharedLib:app:desktop-jni:SharedLib_run_app_desktop_jni
gradlew.bat :examples:SharedLib:app:desktop-ffm:SharedLib_run_app_desktop_ffm
```

## Cross-platform variants

Replace `windows64` with `linux64`, `mac64`, or `macArm` where available.

## Quick compile sanity checks

```powershell
gradlew.bat :jParser:jParser-core:compileJava
gradlew.bat :jParser:jParser-build-tool:compileJava
```

