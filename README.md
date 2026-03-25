<p align="center">
  <h1 align="center">jParser</h1>
  <p align="center">
    A Java code-generation library that bridges C/C++ native code to JVM platforms — desktop, mobile, and web.
  </p>
</p>

<p align="center">
  <a href="https://github.com/xpenatan/jParser/actions/workflows/snapshot.yml"><img src="https://github.com/xpenatan/jParser/actions/workflows/snapshot.yml/badge.svg" alt="Build"></a>
  <a href="https://central.sonatype.com/artifact/com.github.xpenatan.jParser/jParser-core"><img src="https://img.shields.io/maven-central/v/com.github.xpenatan.jParser/jParser-core" alt="Maven Central Version"></a>
  <a href="https://central.sonatype.com/service/rest/repository/browse/maven-snapshots/com/github/xpenatan/jParser/"><img src="https://img.shields.io/badge/snapshot---SNAPSHOT-red" alt="Snapshot"></a>
  <a href="LICENSE"><img src="https://img.shields.io/badge/license-Apache%202.0-blue.svg" alt="License"></a>
</p>

---

## Table of Contents

- [Overview](#overview)
- [How It Works](#how-it-works)
- [Supported Targets](#supported-targets)
- [Code Block Convention](#code-block-convention)
- [WebIDL Bindings](#webidl-bindings)
- [IDLBase API](#idlbase-api)
- [Requirements](#requirements)
- [Getting Started](#getting-started)
- [Libraries Using jParser](#libraries-using-jparser)
- [License](#license)

---

## Overview

Inspired by [gdx-jnigen](https://github.com/libgdx/gdx-jnigen), jParser lets you embed native C/C++ code directly inside Java source files using annotated comment blocks. Each block is translated into target-specific Java source code, enabling a single `lib-base` module to produce separate outputs for **JNI** (desktop & mobile), **FFM** (desktop, Java 22+), and **TeaVM** (web via JS/WASM).

For web targets, jParser uses [Emscripten](https://emscripten.org/) to compile C/C++ into JS/WASM and [TeaVM](https://github.com/konsoletyper/teavm) to generate the corresponding Java-to-JavaScript bridge via `@JSBody` annotations.

## How It Works

jParser consists of two main stages:

### 1. Code Generation

Reads the hand-written Java source in the `lib-base` module — which contains embedded native code blocks — and generates platform-specific Java source for each target:

| Output Module | Target | Description |
|---|---|---|
| `lib-core` | JNI | Generated JNI Java for desktop & mobile |
| `lib-teavm` | TeaVM | Generated `@JSBody`-annotated Java for web |
| `lib-desktop-ffm` | FFM | Generated FFM Java for desktop (Java 22+) |

### 2. Native Compilation

Compiles the C/C++ source into platform-specific native libraries:

| Platform | Toolchain |
|---|---|
| Windows | MinGW64 or MSVC |
| Linux | GCC / G++ |
| macOS | Xcode CLI tools |
| Android | Android NDK |
| Web | Emscripten SDK |

## Supported Targets

| Target | Bridge | Platforms | Java Version |
|---|---|---|--------------|
| **JNI** | Java Native Interface | Windows, Linux, macOS, Android | 8+           |
| **FFM** | Foreign Function & Memory API | Windows, Linux, macOS | 22+          |
| **TeaVM** | JavaScript / WASM | Web browsers | 11+          |

## Code Block Convention

In `lib-base` Java source files, native code is embedded via annotated comment blocks. jParser reads these blocks and generates the appropriate code for each target.

```java
public class MyLib extends IDLBase {

    // JNI native code block — compiled into C++ for desktop & mobile
    /*[-JNI;-NATIVE]
        MyType* obj = (MyType*)this_addr;
        return obj->getValue();
    */
    private static native int internal_native_getValue(long this_addr);

    // TeaVM replacement — generates @JSBody-annotated method for web
    /*[-TEAVM;-REPLACE]
        @org.teavm.jso.JSBody(params = {"this_addr"},
            script = "var jsObj = [MODULE].wrapPointer(this_addr, [MODULE].MyType);"
                   + "return jsObj.getValue();")
        private static native int internal_native_getValue(int this_addr);
    */
}
```

### Available Commands

| Command | Description |
|---|---|
| `-NATIVE` | Inline C/C++ code compiled for the target |
| `-ADD` | Adds code to the generated output |
| `-ADD_RAW` | Adds raw code without processing |
| `-REMOVE` | Removes code from the generated output |
| `-REPLACE` | Replaces the following method with the block content |
| `-REPLACE_BLOCK` | Replaces the following code block |
| `-IDL_SKIP` | Placed on a class comment to skip IDL generation for that class |

## WebIDL Bindings

To reduce the effort of manually porting each method, jParser supports **Emscripten WebIDL**. Define a `.idl` file and jParser automatically generates binding code for all targets.

```idl
interface NormalClass {
    void NormalClass();
    long addIntValue(long value1, long value2);
    static long subIntValue(long value1, long value2);
    attribute long intValue;
    attribute float floatValue;
};
```

This generates fully working Java classes with native bindings for JNI, FFM, and TeaVM — no manual glue code required.

### WebIDL Notes

- **IDL helper classes** (`IDLInt`, `IDLIntArray`, etc.) let you pass primitive pointers to C++. They work across Emscripten, desktop, and mobile.
- **C++ enums** are converted into Java enums, each carrying the integer value from native code.
- **`[Value]` methods** return a cached copy of the object. The cache is overwritten on each call — do not retain references.
- **`[NoDelete]` classes** should not have `dispose()` called. All other classes require explicit disposal.

## IDLBase API

Every native class extends `IDLBase`, which provides common memory-management functionality.

> **Important:** jParser does not automatically dispose C++ objects. You must call `dispose()` when you're done with an object to free native memory. Only objects you create or explicitly own require disposal. Creating and disposing native objects is expensive — avoid doing it every frame.

| Method | Description |
|---|---|
| `ClassName.native_new()` | Creates an empty instance without native data |
| `ClassName.NULL` | Returns a NULL instance — use instead of Java `null` for native parameters |
| `dispose()` | Deletes the native instance (only if owned) |
| `isDisposed()` | Checks whether the native instance has been disposed |
| `native_setVoid(...)` | Sets an integer or long memory address |
| `native_reset()` | Resets the instance to default state |
| `native_takeOwnership()` | Takes ownership, enabling `dispose()` to delete the object |
| `native_releaseOwnership()` | Releases ownership, preventing `dispose()` from deleting |
| `native_hasOwnership()` | Checks whether you own the native instance |
| `native_copy(...)` | Copies memory address and native data from another instance |

> The `native_` prefix is used to avoid naming conflicts with C/C++ methods.

## Requirements

| Requirement | Purpose |
|---|---|
| **JDK 11+** | Building jParser tool modules |
| **JDK 22+** (25 recommended) | FFM modules and FFM-based apps |
| [MinGW64](https://github.com/niXman/mingw-builds-binaries/releases) or [Visual Studio C++](https://visualstudio.microsoft.com/vs/community/) | Windows native builds |
| GCC / G++ | Linux native builds |
| Xcode CLI tools | macOS native builds |
| [Emscripten SDK](https://emscripten.org/) | Web builds (JS/WASM) |

> **Windows (MSVC):** Ensure `vcvarsall.bat` is on your system `PATH`. It is typically located at:
> `C:\Program Files\Microsoft Visual Studio\[Year]\[Edition]\VC\Auxiliary\Build\`

## Getting Started

For a complete working example, refer to the [`examples/TestLib`](examples/TestLib) module.

### Module Layout

jParser projects follow a strict `-base / -build / -core / -teavm` convention:

| Module Suffix | Purpose |
|---|---|
| `lib-base` | Hand-written Java source with embedded native code blocks |
| `lib-build` | Build entry point — configures IDL, targets, runs generation + compilation |
| `lib-core` | **Generated** JNI Java output _(do not hand-edit)_ |
| `lib-teavm` | **Generated** TeaVM Java output _(do not hand-edit)_ |
| `lib-desktop-ffm` | **Generated** FFM Java output _(do not hand-edit)_ |
| `lib-desktop-jni` | Bundles JNI-compiled native libraries into a JAR |
| `lib-android` | Android-specific packaging |

### Build Example: TestLib

```bash
# 1. Build idl-helper (required once)
./gradlew :idl-helper:idl-helper-build:idl_helper_build_project_jni_windows64

# 2. Generate code + compile native library
./gradlew :examples:TestLib:lib:lib-build:TestLib_build_project_jni_windows64

# 3. Run the desktop app
./gradlew :examples:TestLib:app:desktop-jni:TestLib_run_app_desktop
```

> Replace `windows64` with `linux64`, `mac64`, or `macArm` for other platforms.
> Replace `jni` with `ffm` for Foreign Function & Memory API targets.

## Libraries Using jParser

| Library | Description | Status |
|---|---|---|
| [jWebGPU](https://github.com/xpenatan/jWebGPU) | WebGPU bindings for Java | Active |
| [xImGui](https://github.com/xpenatan/xImGui) | Dear ImGui bindings for Java | Active |
| [xJolt](https://github.com/xpenatan/xJolt) | Jolt Physics bindings for Java | Active |
| [xLua](https://github.com/xpenatan/XLua) | Lua bindings for Java | Active |
| [xBullet](https://github.com/xpenatan/xBullet) | Bullet Physics bindings for Java | Active |
| [gdx-box2d](https://github.com/xpenatan/gdx-box2d) | Box2D bindings for libGDX | Inactive |
| [gdx-physx](https://github.com/xpenatan/gdx-physx) | PhysX bindings for libGDX | Inactive |

## License

jParser is licensed under the [Apache License 2.0](LICENSE).
