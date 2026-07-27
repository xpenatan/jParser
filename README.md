<h1 align="center">jParser</h1>
<p align="center">
  Generate Java bindings for C/C++ libraries across desktop, mobile, web, and native TeaVM applications.
</p>

<p align="center">
  <a href="https://github.com/xpenatan/jParser/actions/workflows/snapshot.yml"><img src="https://github.com/xpenatan/jParser/actions/workflows/snapshot.yml/badge.svg" alt="Build"></a>
  <a href="https://central.sonatype.com/artifact/com.github.xpenatan.jParser/gen-core"><img src="https://img.shields.io/maven-central/v/com.github.xpenatan.jParser/gen-core" alt="Maven Central Version"></a>
  <a href="https://central.sonatype.com/service/rest/repository/browse/maven-snapshots/com/github/xpenatan/jParser/"><img src="https://img.shields.io/badge/snapshot---SNAPSHOT-red" alt="Snapshot"></a>
  <a href="LICENSE"><img src="https://img.shields.io/badge/license-Apache%202.0-blue.svg" alt="License"></a>
</p>

## Overview

jParser turns a shared Java API, WebIDL definitions, and embedded native code blocks into bindings for multiple runtimes:

| Target | Bridge | Platforms | Java requirement |
|---|---|---|---|
| JNI | Java Native Interface | Windows, Linux, macOS, Android | Java 8+ API |
| FFM | Foreign Function & Memory API | Windows, Linux, macOS | Java 22+; 25 recommended |
| TeaVM web | JavaScript / WebAssembly | Web browsers | Java 17+ tooling |
| TeaVM C | TeaVM native C imports | Windows, Linux, macOS, Android, iOS | Java 17+ tooling |

A typical project has a hand-written `base` API and `builder` configuration. jParser generates the bridge-agnostic `core` API and target implementations, then invokes the appropriate native toolchain to build or package platform artifacts. For iOS TeaVM C, jParser produces static library slices and portable CMake resources; the application framework or consumer owns the launcher, Xcode project, bundle metadata, signing, and final app packaging.

Web builds use [Emscripten](https://emscripten.org/) and [TeaVM](https://github.com/konsoletyper/teavm).

Repository development is fully local: the root build owns the `jParser`
modules and the `:gradle-plugin` project. The plugin compiles against the local
`gen-build`, `gen-idl`, and `gen-build-tool` projects. The plugin validation
fixtures use the separate `examples/settings.gradle.kts` consumer build, which
includes the root build for plugin resolution. Changing the generator, plugin,
and fixtures together therefore requires no Maven publication.

## Quick Start

[`examples/TestLib`](examples/TestLib) is the smallest complete project to follow. From the repository root, a Windows JNI build is:

```powershell
.\gradlew.bat :jParser:runtime:builder:runtime_helper_build_project_windows64_jni
.\gradlew.bat :examples:TestLib:lib:builder:TestLib_build_project_windows64_jni
.\gradlew.bat :examples:TestLib:app:platforms:desktop-jni:TestLib_run_app_desktop_jni
```

Use `./gradlew` on Linux or macOS. See the [getting-started guide](docs/getting-started.md) for the module layout and FFM example, or the [command reference](docs/commands.md) for every platform target.

## Requirements

Install only the toolchains required by the targets you build:

| Requirement | Used for |
|---|---|
| JDK 11+ | Core jParser tooling |
| JDK 17+ | TeaVM web and TeaVM C tooling |
| JDK 22+ (25 recommended) | FFM modules and applications |
| [Visual Studio C++](https://visualstudio.microsoft.com/vs/community/) | Windows native builds |
| GCC / G++ | Linux native builds |
| Xcode command-line tools | macOS and iOS native builds |
| Android NDK | Android native builds |
| [Emscripten SDK](https://emscripten.org/) | Web JavaScript/WebAssembly builds |

Windows builds auto-detect `vcvarsall.bat` from `VCVARSALL_PATH`, `JPARSER_VCVARSALL`, `PATH`, Visual Studio environment variables, or `vswhere.exe`. Override it with `-Djparser.vcvarsall=<path>` when necessary.

## Documentation

| Guide | Contents |
|---|---|
| [Getting started](docs/getting-started.md) | Project layout and the first TestLib builds |
| [Binding authoring](docs/binding-authoring.md) | Native directive blocks, WebIDL, and `IDLBase` ownership |
| [TeaVM C](docs/teavm-c.md) | Linkage modes, packaged dependencies, consumer metadata, and MSVC runtime selection |
| [Fat native bundles](docs/native-bundles.md) | `_resources` publications, variant resolution, final linking, and loading |
| [Command reference](docs/commands.md) | Build, run, packaging, and benchmark commands |
| [Architecture](docs/architecture.md) | Generator pipeline, module map, and runtime internals |
| [Runtime Maven artifacts](docs/runtime-maven-artifacts.md) | Published modules, classifiers, and native resource layouts |
| [Contributor workflow](docs/workflows.md) | Editing, generation, and verification rules |

## Projects Using jParser

| Project | Description | Status |
|---|---|---|
| [jWebGPU](https://github.com/xpenatan/jWebGPU) | WebGPU bindings for Java | Active |
| [jImGui](https://github.com/xpenatan/jImGui) | Dear ImGui bindings | Active |
| [jJolt](https://github.com/xpenatan/jJolt) | Jolt Physics bindings | Active |
| [jLua](https://github.com/xpenatan/jLua) | Lua bindings | Active |
| [jBox3D](https://github.com/xpenatan/jBox3D) | Box3D bindings | Active |
| [jBox2D](https://github.com/xpenatan/jBox2D) | Box2D bindings | Active |
| [jBullet](https://github.com/xpenatan/jBullet) | Bullet bindings | Inactive |
| [gdx-physx](https://github.com/xpenatan/gdx-physx) | PhysX bindings for libGDX | Inactive |

## Support

If jParser is useful to you, consider [sponsoring its development](https://github.com/sponsors/xpenatan).

## License

jParser is available under the [Apache License 2.0](LICENSE).
