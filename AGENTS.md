# AGENTS.md — jParser

## Project Overview

jParser is a Java code-generation and C/C++ compilation library that bridges native code to JVM platforms (desktop, mobile, web). It reads Java source files containing embedded native code blocks, then generates platform-specific Java source for **JNI** (desktop/mobile) and **TeaVM** (web/WASM) targets. It also supports **WebIDL**-driven automatic binding generation.

### Context Resumption & State Persistence

If you are asked to continue a task or if this chat has been restored from a backup, **always check for `LOCAL_AGENT.md`** in the project root.

**This file is the single source of truth for the current state machine.** It must be updated **BEFORE** every significant action.

### Agent Workflow for Persistence:
1. **Analyze**: Understand the user request and existing codebase. **NEVER guess code structures or property names.** You must verify the existence of classes, methods, and fields by reading the source files. (e.g., Do not assume a variable or a method exists; check the class structure first).
2. **Sync State**: Read `LOCAL_AGENT.md` to understand where the previous session left off.
3. **Plan & Commit**: Document any new architectural decisions, sub-tasks, or file modifications in `LOCAL_AGENT.md` **before** writing code.
4. **After planning:**: Always ask the user if can execute the plan. User may ask questions about the plan. If user approves, proceed to execute. If user asks for changes, update the plan and repeat step 3.
5. **Execute**: Perform file modifications, terminal commands, etc.
6. **Update Progress**: After a task (or significant sub-task) is completed or verified, update the "Current Progress" and "Next Task" sections in `LOCAL_AGENT.md`.
7. **Verify**: Always run compilation to ensure the state is stable before ending the turn.

This ensures that if the session is interrupted, the next agent has a perfect "snapshot" of the state machine, including why decisions were made and which files were touched.

## Architecture

### Core Pipeline (`BuilderTool.build()` in `jParser/jParser-build-tool`)
1. **IDL Parsing** — `IDLReader` reads `.idl` files from `lib-build/src/main/cpp/`
2. **Code Generation (JNI)** — `CppCodeParser` (extends `IDLDefaultCodeParser`) reads `lib-base/src/main/java` source, generates JNI Java into `lib-core/src/main/java`
3. **Code Generation (TeaVM)** — `TeaVMCodeParser` generates TeaVM/JS Java into `lib-teavm/src/main/java`
4. **Native Compilation** — `JBuilder.build()` compiles C/C++ for each platform target via `BuildMultiTarget`

### Module Layout (follows a strict `-base/-build/-core/-teavm` convention)

| Suffix | Purpose | Java target |
|---|---|---|
| `lib-base` | Hand-written Java source with embedded `/*[-JNI;-NATIVE]*/` and `/*[-TEAVM;-REPLACE]*/` code blocks | Java 8 |
| `lib-build` | `BuildLib.main()` entry point — configures IDL, targets, runs generation + compilation | Java 11 |
| `lib-core` | **Generated** JNI Java output (do not hand-edit) | Java 11 |
| `lib-teavm` | **Generated** TeaVM Java output (do not hand-edit) | Java 11 |
| `lib-desktop` | Runtime loader for desktop | — |
| `lib-android` | Android-specific packaging | — |

This pattern repeats across `jParser/`, `idl-helper/`, `loader/`, and `examples/`.

### Key Modules

- **`jParser/jParser-core`** — `JParser.generate()` entry point; uses JavaParser to parse/transform Java ASTs. `CodeParser` interface → `DefaultCodeParser` → `IDLDefaultCodeParser`.
- **`jParser/jParser-cpp`** — `CppCodeParser` (header `"JNI"`) generates JNI glue code + `NativeCPPGenerator` emits C++ `.cpp` files.
- **`jParser/jParser-teavm`** — `TeaVMCodeParser` (header `"TEAVM"`) generates `@JSBody`-annotated methods for TeaVM.
- **`jParser/jParser-idl`** — IDL file parser, class model (`IDLClass`, `IDLMethod`, `IDLAttribute`), and code generation parsers.
- **`jParser/jParser-build`** — `JBuilder`, `BuildConfig`, platform targets (`EmscriptenTarget`, `WindowsMSVCTarget`, `LinuxTarget`, etc.).
- **`idl/idl-core`** — `IDLBase` parent class for all native objects (memory management, ownership, dispose).

## Code Block Convention

In `lib-base` Java source, native code is embedded via block comments with headers:
```java
/*[-JNI;-NATIVE]
    MyType* obj = (MyType*)this_addr;
    obj->doSomething();
*/
private static native void internal_native_doSomething(long this_addr);

/*[-TEAVM;-REPLACE]
    @org.teavm.jso.JSBody(params = {"this_addr"}, script = "...")
    private static native void internal_native_doSomething(int this_addr);
*/
```
Commands: `-ADD`, `-ADD_RAW`, `-REMOVE`, `-REPLACE`, `-REPLACE_BLOCK`, `-NATIVE`. Use `-IDL_SKIP` on a class comment to prevent IDL generation for that class.

## Requirements

- **JDK 11+** for building the jParser tool modules
- **JDK 22+** (25 recommended) for FFM modules and running FFM-based apps
- **Gradle** — wrapper included (`./gradlew` / `gradlew.bat`)
- **Windows native builds**: MinGW64 **and/or** Visual Studio C++ (`vcvarsall.bat` must be on PATH)
- **Linux native builds**: GCC / G++ toolchain
- **macOS native builds**: Xcode command-line tools
- **Web builds**: Emscripten SDK (`EMSDK` env var)

## Build & Run

All commands are run from the **project root**. Use `./gradlew` on Linux/macOS, `gradlew.bat` on Windows.

### 1. Build the IDL Helper library (required before examples)

The idl-helper provides the `IDLBase` runtime for all native-bound objects. It must be code-generated and native-compiled before examples can run.

```sh
# Step 1 — Generate JNI/TeaVM/FFM Java code + compile native library for your platform
# JNI (pick your platform):
./gradlew :idl-helper:idl-helper-build:idl_helper_build_project_windows64
./gradlew :idl-helper:idl-helper-build:idl_helper_build_project_linux64
./gradlew :idl-helper:idl-helper-build:idl_helper_build_project_mac64
./gradlew :idl-helper:idl-helper-build:idl_helper_build_project_macArm

# FFM (pick your platform):
./gradlew :idl-helper:idl-helper-build:idl_helper_build_project_ffm_windows64
./gradlew :idl-helper:idl-helper-build:idl_helper_build_project_ffm_linux64
./gradlew :idl-helper:idl-helper-build:idl_helper_build_project_ffm_mac64
./gradlew :idl-helper:idl-helper-build:idl_helper_build_project_ffm_macArm

# All JNI platforms at once:
./gradlew :idl-helper:idl-helper-build:idl_helper_build_project_all
```

### 2. Build example: TestLib

TestLib is the primary test/example library. Building it generates Java source code into `lib-core`, `lib-teavm`, and `lib-ffm`, then compiles native C/C++ into platform DLLs/shared-libs.

```sh
# Step 1 — Generate Java code + compile native for your platform (JNI):
./gradlew :examples:TestLib:lib:lib-build:TestLib_build_project_windows64
./gradlew :examples:TestLib:lib:lib-build:TestLib_build_project_linux64
./gradlew :examples:TestLib:lib:lib-build:TestLib_build_project_mac64
./gradlew :examples:TestLib:lib:lib-build:TestLib_build_project_macArm

# All JNI platforms:
./gradlew :examples:TestLib:lib:lib-build:TestLib_build_project_all

# FFM (generates lib-ffm Java code + compiles FFM native):
./gradlew :examples:TestLib:lib:lib-build:TestLib_build_project_ffm_windows64
./gradlew :examples:TestLib:lib:lib-build:TestLib_build_project_ffm_linux64
./gradlew :examples:TestLib:lib:lib-build:TestLib_build_project_ffm_mac64
./gradlew :examples:TestLib:lib:lib-build:TestLib_build_project_ffm_macArm

# Generate FFM Java code only (no native compilation):
./gradlew :examples:TestLib:lib:lib-build:TestLib_build_project_ffm

# Step 2 — Run the desktop app:
./gradlew :examples:TestLib:app:desktop:TestLib_run_app_desktop
```

### 3. Build example: SharedLib (multi-library)

SharedLib demonstrates two libraries (libA + libB) where libB depends on libA. **Build libA first**, then libB.

```sh
# libA — JNI:
./gradlew :examples:SharedLib:libA:lib-build:LibA_build_project_windows64
# libA — FFM:
./gradlew :examples:SharedLib:libA:lib-build:LibA_build_project_ffm_windows64

# libB — JNI (after libA):
./gradlew :examples:SharedLib:libB:lib-build:LibB_build_project_windows64
# libB — FFM (after libA):
./gradlew :examples:SharedLib:libB:lib-build:LibB_build_project_ffm_windows64

# Run SharedLib desktop app:
./gradlew :examples:SharedLib:app:desktop:SharedLib_run_app_desktop
```

Replace `windows64` with `linux64`, `mac64`, or `macArm` for other platforms.

### 4. JNI vs FFM Benchmarks

Run micro-benchmarks comparing JNI and FFM bridge overhead. **Requires both JNI and FFM native DLLs to be built first** (see steps 2–3 above).

```sh
# Run both benchmarks and print a comparison table
./gradlew :examples:TestLib:app:desktop:TestLib_benchmark_compare

# Run only JNI benchmark (saves CSV to build/benchmark/benchmark_jni.csv)
./gradlew :examples:TestLib:app:desktop:TestLib_benchmark_jni

# Run only FFM benchmark (saves CSV to build/benchmark/benchmark_ffm.csv)
./gradlew :examples:TestLib:app:desktop:TestLib_benchmark_ffm
```

### 5. JNI vs FFM FPS Benchmarks

Measures how native bridge overhead affects frame rate. Each frame executes a fixed number of native calls, then returns to let GDX render. Reports average and minimum FPS per scenario.

```sh
# Run both FPS benchmarks and print a comparison table
./gradlew :examples:TestLib:app:desktop:TestLib_fps_benchmark_compare

# Run only JNI FPS benchmark (saves CSV to build/benchmark/fps_benchmark_jni.csv)
./gradlew :examples:TestLib:app:desktop:TestLib_fps_benchmark_jni

# Run only FFM FPS benchmark (saves CSV to build/benchmark/fps_benchmark_ffm.csv)
./gradlew :examples:TestLib:app:desktop:TestLib_fps_benchmark_ffm
```

### Build order summary (from scratch on Windows)

```sh
# 1. Build idl-helper native (both JNI + FFM)
./gradlew :idl-helper:idl-helper-build:idl_helper_build_project_windows64
./gradlew :idl-helper:idl-helper-build:idl_helper_build_project_ffm_windows64

# 2. Build TestLib native (both JNI + FFM)
./gradlew :examples:TestLib:lib:lib-build:TestLib_build_project_windows64
./gradlew :examples:TestLib:lib:lib-build:TestLib_build_project_ffm_windows64

# 3. Run the desktop app
./gradlew :examples:TestLib:app:desktop:TestLib_run_app_desktop

# 4. Run throughput benchmarks
./gradlew :examples:TestLib:app:desktop:TestLib_benchmark_compare

# 5. Run FPS benchmarks
./gradlew :examples:TestLib:app:desktop:TestLib_fps_benchmark_compare
```

## Conventions

- **Version management**: `gradle.properties` holds the version; `LibExt.kt` in `buildSrc/` resolves it. Snapshots use `"-SNAPSHOT"`, releases use the property value.
- **Publishing**: The `publish.gradle.kts` plugin configures all library modules listed in `libProjects`. Use `publishRelease` or `publishTestRelease` tasks.
- **Generated code is not hand-edited**: `lib-core/`, `lib-teavm/`, and `lib-ffm/` directories contain generated output with a "Do not make changes" header.
- **IDL files** live at `lib-build/src/main/cpp/<LibName>.idl`. Custom C++ glue code goes in `lib-build/src/main/cpp/custom/`.
- **IDLBase** is the parent of all native-bound classes. Memory must be manually managed via `dispose()`. Use `ClassName.NULL` instead of Java `null` for native parameters.
- **Dependencies**: JavaParser (`3.26.1`) for AST manipulation, TeaVM (`0.13.0`) for web target, JUnit 4 for tests.
- **Native bridge selection**: Desktop modules choose `lib-core` (JNI) or `lib-ffm` (FFM) via the `implementation` dependency in `app/desktop/build.gradle.kts`. Only one should be active at a time.

