# TeaVM C Guide

TeaVM C generates native imports plus portable C/C++ build resources for applications compiled through TeaVM's C backend. This guide covers public configuration and deployment. See [Architecture](architecture.md#teavm-c) for the generated ABI, dispatch implementation, and complete resource layout.

## Linkage Modes

`TeaVMCLinkage` controls how a generated binding reaches its native implementation:

| Mode | Native resolution | Deployment |
|---|---|---|
| `STATIC` | Links the packaged archive into the final native target; this is the default | Application executable, subject to unrelated shared dependencies |
| `SHARED_LINKED` | Links a DLL import library, shared object, or dylib and lets the operating-system loader resolve it at process startup | Application plus the matching shared library |
| `RUNTIME_LOADED` | Opens the shared library on demand, validates its generated versioned API table, and binds the dispatch shim | Application plus one runtime-selectable shared library per logical binding |

Configure the mode through the Gradle plugin:

```kotlin
import com.github.xpenatan.jParser.builder.tool.TeaVMCLinkage

jParser {
    teaVMCLinkage.set(TeaVMCLinkage.RUNTIME_LOADED)
}
```

Manual builders set the same value before constructing `BuildToolOptions`:

```java
import com.github.xpenatan.jParser.builder.tool.BuildToolOptions;
import com.github.xpenatan.jParser.builder.tool.TeaVMCLinkage;

BuildToolOptions.BuildToolParams params = new BuildToolOptions.BuildToolParams();
params.teaVMCLinkage = TeaVMCLinkage.SHARED_LINKED;
BuildToolOptions options = new BuildToolOptions(params, args);
```

The command-line runner also accepts `-Djparser.teaVMCLinkage=STATIC`, `SHARED_LINKED`, or `RUNTIME_LOADED`.

## Runtime-Loaded Libraries

`loader-c` substitutes the normal `JParserLibraryLoader` API. Windows uses `LoadLibraryExW` and `GetProcAddress`; Linux, macOS, and Android use `dlopen` and `dlsym`. A logical binding can be loaded only once, and a successfully loaded library remains open for the process lifetime.

`RUNTIME_LOADED` normally derives the platform filename from the logical library name. Set `JParserLibraryLoaderOptions.fileName` to select an exact physical payload name; `path` remains its containing directory:

```java
JParserLibraryLoaderOptions options = new JParserLibraryLoaderOptions();
options.path = "plugins";
options.fileName = "webgpu_dawn64.dll";
JParserLibraryLoader.load("webgpu", options, listener);
```

Shared binaries inside dependency jars are build resources, not files that an operating-system loader can open. A launcher must extract and deploy them to the filesystem. iOS supports `STATIC` linkage only: native code must be bundled with and signed as part of the application, and arbitrary downloaded plugins are outside that model.

## Packaged Native Dependencies

Producer native settings and final-consumer requirements serve different purposes:

- Methods directly on `target(...)` or `targetVariant(...)`, including `headerDir`, `compileFlag`, and `staticLinkerInput`, configure how the producer builds its library. They may use producer-machine paths and are not exported.
- The nested `consumer { ... }` block describes portable inputs required by the final TeaVM C application. Its paths are relative to `external_cpp/jparser/<library>/native/<platform>` and must never refer to the producer's filesystem.

One variant of a binding with mutually exclusive native implementations can be declared as follows:

```kotlin
import com.github.xpenatan.jParser.gradle.JParserTargets

jParser {
    native {
        targetVariant(JParserTargets.LINUX64_TEAVM_C, "backendA") {
            // Producer compiler and linker inputs belong at this level.

            consumer {
                selectorResource("include/backend_a/api.h")
                headerDir("include")
                compileDefinition("MYLIB_BACKEND_A=1")
                staticLibrary("deps/libbackend_a.a", "MYLIB_BACKEND_A_LIBRARY")
                staticLinkLibrary("pthread")
            }
        }
    }
}
```

jParser collects every enabled TeaVM C consumer declaration and writes the selection/link logic into the binding's existing generated `jparser_<library>_teavm_c.cmake`. Native artifacts package headers and libraries; they do not need additional ordered CMake hooks.

At CMake configure time, all `selectorResource(...)` entries for a candidate must exist. Exactly one variant must match the current platform. Missing and ambiguous native artifacts fail with an explicit diagnostic.

| Method | Final-application effect |
|---|---|
| `selectorResource(path)` | Requires a resource whose presence identifies this variant |
| `headerDir(path)` | Adds a packaged include directory |
| `compileDefinition(value)` | Adds a compile definition |
| `compileFlag(value)` | Passes a compiler option through unchanged |
| `staticLibrary(path, overrideVariable)` | Links a packaged archive in `STATIC` mode; the optional CMake variable overrides its location |
| `staticLinkLibrary(value)` | Adds a system or transitive library in `STATIC` mode |
| `staticLinkerFlag(value)` | Adds a linker option in `STATIC` mode |

Automatic consumer selection supports Windows x64, Linux x64, macOS x64/arm64, Android ABI resource roots, and iOS device/simulator archive slices.

## iOS Static Library Contract

The `ios_teavm_c` producer target builds three static slices with the active Xcode toolchain:

| SDK | Architecture | Resource directory |
|---|---|---|
| `iphoneos` | `arm64` | `ios/device/arm64` |
| `iphonesimulator` | `arm64` | `ios/simulator/arm64` |
| `iphonesimulator` | `x86_64` | `ios/simulator/x86_64` |

Each slice is named `lib<Library>64_.a`. The `runtime-ios-c` artifact, and the equivalent `<Library>-ios-c` binding artifact, packages all three slices below `external_cpp/jparser/<library>/native/ios/**`. Keeping the slices together lets Xcode switch simulator architecture without resolving another Gradle dependency.

Apple SDKs do not provide the C11 `<uchar.h>` API expected by TeaVM's generated C runtime. The `runtime-ios-c` artifact packages a compatible header at `external_cpp/jparser/runtime/custom/uchar.h`. The existing generated runtime CMake hook already adds that custom directory to the TeaVM C target's include paths, so consumers that extract the artifact's `external_cpp/**` resources and include the normal runtime hook receive the compatibility layer automatically. No additional iOS hook or application-private copy is required.

TeaVM 0.15.0 also emits a Unix fiber runtime that uses POSIX realtime timers and signals, which Apple platforms do not provide. Until the first TeaVM release containing [the upstream Apple runtime fix](https://github.com/konsoletyper/teavm/commit/9c99e0e67d99cbc3a3736bc34a1cc9222ed82059), `runtime-ios-c` packages its `definitions.h` and `fiber.c` backport under `external_cpp/jparser/runtime/teavm/ios`. An iOS application build overlays those two files onto the generated TeaVM C source directory after generation and before native compilation. The TestLib and SharedLib custom emulator preparation tasks demonstrate this source-preparation step; it does not require another CMake hook or change jParser's launcher boundary.

The generated CMake hook selects one slice from `CMAKE_OSX_SYSROOT` and `CMAKE_OSX_ARCHITECTURES`. An iOS consumer must select exactly one architecture for a build. Device builds accept ARM64; simulator builds accept ARM64 or x86_64. The hook rejects `SHARED_LINKED` and `RUNTIME_LOADED` on iOS, links an imported static target, and appends that target to `TEAVM_IOS_STATIC_DEPENDENCY_TARGETS` when the consumer defines the list.

jParser stops at this integration boundary. It generates TeaVM C bindings/resources, invokes `xcrun` to compile the static libraries, and packages the slices. It does not generate an application launcher, UI, `Info.plist`, Xcode project, signing configuration, or final bundle. Those belong to gdx-teavm, libfdx, or the consuming application. The committed TestLib and SharedLib `ios-c` applications are handwritten custom emulators that render their result in a real iPhone Simulator and verify the contract in GitHub Actions.

## Windows MSVC Runtime

The Windows CRT family is independent from TeaVM C linkage. A static library or DLL can be compiled with either `/MT` or `/MD`, but all static libraries participating in one final link must use compatible runtime settings.

jParser does not choose a runtime by default. A producer can pass ordinary flags per target or variant:

```kotlin
import com.github.xpenatan.jParser.gradle.JParserTargets

jParser {
    native {
        targetVariant(JParserTargets.WINDOWS64_TEAVM_C, "mt") {
            compileFlag("/MT")
        }
        targetVariant(JParserTargets.WINDOWS64_TEAVM_C, "md") {
            compileFlag("/MD")
        }
    }
}
```

Variants use separate `build/c++/libs/<variant>/...` and `build/c++/target/<variant>/...` trees. There is no MSVC-specific jParser DSL: `compileFlag(...)` and `compileFlags` pass raw compiler options through unchanged, and other platforms use the same hooks for their toolchain options.

TeaVM C applications select the runtime through standard CMake configuration, such as `-DCMAKE_MSVC_RUNTIME_LIBRARY=MultiThreadedDLL` for MD or `MultiThreaded` for MT. Generated jParser integration never changes that value. It only inspects the consumer's selection to locate the matching `md` or `mt` packaged payload; when no value is set, lookup follows CMake's default MD family.

## Related References

- [Architecture](architecture.md#teavm-c) documents the generated ABI, CMake variables, platform payload names, and gdx-teavm adapter behavior.
- [Runtime Maven artifacts](runtime-maven-artifacts.md) documents published TeaVM C modules and native resource classifiers.
- [Command reference](commands.md) lists TeaVM C build and packaging tasks.
