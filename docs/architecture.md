# Architecture Reference

## Core Build Pipeline

Primary orchestration is in `jParser/jParser-build-tool` via `BuilderTool.build()`:

1. IDL parsing (`IDLReader`) from the build module's `src/main/cpp/*.idl`
2. Core API generation (`IDLDefaultCodeParser`) into the configured core module
3. JNI generation (`CppCodeParser`) into the configured JNI module
4. TeaVM web generation (`TeaVMCodeParser` from `gen-web`) into the configured web module
5. TeaVM C generation (`TeaVMCCodeParser` from `gen-c`) into the configured C module plus `TeaVMCGlue.cpp`
6. FFM generation (`FFMCodeParser`) into the configured FFM module
7. Native compile (`JBuilder.build()`) via platform targets

## Module Conventions

- `base`: handwritten Java with target-specific comment blocks in examples
- `builder`: Gradle entry for generation + native build in examples
- `core`: generated bridge-agnostic public API in examples
- `shared/<Lib>-jni`: generated JNI Java shared by desktop and Android JNI examples
- `shared/<Lib>-c`: generated `gen.c.*` TeaVM C implementations and portable build resources shared by TeaVM C consumers
- `desktop/<Lib>-desktop-jni`: desktop JNI native packaging with a dependency on `shared/<Lib>-jni`
- `desktop/<Lib>-desktop-ffm`: generated FFM Java + desktop FFM natives
- `desktop/<Lib>-desktop-c`: TeaVM C desktop native payloads
- `android/<Lib>-android`: Android JNI packaging with a dependency on `shared/<Lib>-jni`
- `android/<Lib>-android-c`: Android TeaVM C native packaging
- `ios/<Lib>-ios-c`: iOS TeaVM C static archives for device ARM64 and simulator ARM64/x86_64
- `web/<Lib>-web`: generated TeaVM web output

Runtime modules mirror the example and binding layout:

- `runtime/base`: handwritten runtime helper source published as `runtime-base`.
- `runtime/builder`: generator and native build driver.
- `runtime/core`: public/shared runtime API published as `runtime-core`.
- `runtime/shared/runtime-jni`: generated JNI Java shared by desktop and Android, published as Java-only `runtime-jni`.
- `runtime/shared/runtime-c`: generated `gen.c.*` TeaVM C implementations, the C substitution service, and portable build resources published as `runtime-c`; it exposes `runtime-core` as the public API.
- `runtime/desktop/runtime-desktop-jni`: desktop JNI packaging published as `runtime-desktop-jni` with a dependency on `runtime-jni`, plus native-only split artifacts such as `runtime-desktop-jni_windows_x64`.
- `runtime/desktop/runtime-desktop-ffm`: generated FFM Java and desktop FFM native payloads, published as `runtime-desktop-ffm`.
- `runtime/desktop/runtime-desktop-c`: desktop TeaVM C native-only split artifacts such as `runtime-desktop-c_windows_x64`.
- `runtime/web/runtime-web`: generated TeaVM web output and WebAssembly payloads, published as `runtime-web`.
- `runtime/android/runtime-android`: Android JNI packaging published as `runtime-android` with a dependency on `runtime-jni`, plus ABI payload artifacts such as `runtime-android_arm64_v8a`.
- `runtime/android/runtime-android-c`: Android TeaVM C packaging published as `runtime-android-c` plus ABI payload artifacts such as `runtime-android-c_arm64_v8a`.
- `runtime/ios/runtime-ios-c`: iOS TeaVM C entry artifact published as `runtime-ios-c`; it depends on `runtime-c` and packages all supported static archive slices.

Loader modules provide one public API with target substitutions:

- `loader/loader-core`: `JParserLibraryLoader`, its listener, and shared loader options used by the public binding loaders.
- `loader/loader-c`: TeaVM C's `emu.c` loader implementation plus the portable C/C++ loader header and source. `runtime-c` and generated binding C artifacts expose this module to TeaVM C applications.
- `loader/loader-web`: the TeaVM web loader implementation.

Split runtime Gradle project paths use the same artifact-style leaf names as the folders. Example split modules follow the same pattern, such as `:examples:TestLib:lib:shared:TestLib-jni` at `examples/TestLib/lib/shared/TestLib-jni` and `:examples:TestLib:lib:desktop:TestLib-desktop-jni` at `examples/TestLib/lib/desktop/TestLib-desktop-jni`.

`runtime-web` owns jParser's TeaVM web substitution service. Binding web modules should depend on `runtime-web`; the runtime policy maps any class to `emu.web.<original-class>` or `gen.web.<original-class>` only when that replacement class is present on the TeaVM classpath. The `emu.web` rule is evaluated before `gen.web`, so explicit emulation wins over generated substitutions. `loader-web` contains the web loader implementation classes but does not register a TeaVM substitution service itself.

`runtime/shared/runtime-c` owns jParser's TeaVM C substitution service. Public classes remain in `runtime-core` and each binding's `core` artifact. TeaVM C generation writes target implementations under `gen.c.<original-package>`, so the public API and its target implementation can coexist on the same classpath without duplicate classes. Binding C modules should expose their public `core` artifact and depend on `runtime-c`; the runtime policy maps any public class to `emu.c.<original-class>` or `gen.c.<original-class>` only when that replacement class is present on the TeaVM classpath. The `emu.c` rule is evaluated before `gen.c`, so explicit emulation wins over generated substitutions.

Example app modules in examples use:

- `app:core` for shared app code.
- `app:assets` where an example has shared assets.
- `app:platforms:desktop-jni`
- `app:platforms:desktop-ffm`
- `app:platforms:desktop-c`
- `app:platforms:web`
- `app:platforms:android`
- `app:platforms:android-c`
- `app:platforms:ios-c` for the handwritten TestLib/SharedLib custom iOS emulator applications; they are not generated by jParser.

Pattern repeats across `examples/`, `idl/`, `loader/`, and `jParser/` modules
(see the root `settings.gradle.kts`).

## Gradle Plugin Support

`gradle-plugin` is an included build that publishes the Maven artifact `com.github.xpenatan.jParser:jparser-gradle-plugin` and plugin id `com.github.xpenatan.jParser`.

The plugin is scoped to build-module orchestration: it creates one task namespace with `jParser_generate` and platform build tasks such as `jParser_build_windows64_jni`. The tasks reuse `BuilderTool`, `BuildToolOptions`, `BuildMultiTarget`, and the platform target classes through `JParserBuildRunner` and `DefaultBuildTargetFactory` in `jParser:gen:gen-build-tool`. Directly invoking `jParser_generate` generates core plus every configured binding family: JNI, FFM, TeaVM web, and TeaVM C. Each regular or variant `jParser_build_*` task generates core plus exactly the binding family required by its platform target and does not invoke the aggregate `jParser_generate` task. Native build task registration follows explicit `native { target(...) }` and `native { targetVariant(...) }` declarations when any are present; module suffixes are only used as the fallback for builds with no explicit native target list.

Symbol naming is configured with the typed build-tool enum `JParserSymbolNameMode` (`DEFAULT` or `OBFUSCATED`) for `jniSymbolNameMode`, `ffmSymbolNameMode`, and `teaVMCSymbolNameMode`; plugin build scripts must not set these values with raw strings.

IDL method renaming is configured by passing an `IDLRenaming` callback to the plugin with `idlRenaming(...)`. This is the same hook used by manual `BuilderTool.build` calls, so builds can rename methods, enums, or packages without rewriting source IDL files. `IDLMethod.name` remains the original IDL/native method name; `IDLMethod.bindingName` stores the generated Java/API method name after explicit IDL `Rename`, overload suffix cleanup, and `IDLRenaming`. Web `@JSBody` native-call generation must call the original exported JavaScript method name from `IDLMethod.name`, not the renamed Java/API binding name.

Native source selection is controlled by `JParserTargetHooks` in `JParserExtension.kt`. By default, each target compiles the parsed source tree with a recursive `**.cpp` glob and the build module's `src/main/cpp/custom/*.cpp` helper sources. Set `includeDefaultSources` or `includeCustomSources` to `false` when a target should use only explicit `cppInclude(...)` globs, for example when binding a C library, linking prebuilt artifacts, or avoiding platform-only helper files.

Native platform targets are selected with the typed `JParserTargets` enum, for example `target(JParserTargets.WINDOWS64_JNI) { ... }`. The public DSL does not accept raw platform target strings, so misspelled target names fail at build-script compilation time. Native target variants are configured with `targetVariant(JParserTargets.WINDOWS64_JNI, variantName) { ... }`. The plugin registers tasks named `jParser_build_<targetName>_<variantName>` and passes the original target-name string to the build tool, so generation and platform selection stay the same while include/link hooks can differ per variant. When a target has one or more variants, the unsuffixed root task `jParser_build_<targetName>` is not registered, even if base target hooks are also configured for that target. Variants default `outputDirectoryPrefix` to the variant name and write native outputs under `build/c++/libs/<variant>/...` and intermediates under `build/c++/target/<variant>/...`. This is for mutually exclusive backends of the same platform target, such as `windows64_jni_wgpu` and `windows64_jni_dawn`. By default variant hooks do not inherit the base target hooks; set `includeBaseTargetHooks` to `true` only when the variant should extend the base target instead of replacing its include/link inputs. Android variants expose the same typed and string `androidTarget(...)` ABI hooks as regular Android targets. Their ABI hooks replace base ABI hooks by default, or are applied after base ABI hooks when `includeBaseTargetHooks` is enabled.

Compiler and toolchain policy stays in the generic global, target, variant, and per-ABI hooks. jParser does not model or inject an MSVC runtime choice: a Windows producer may pass `/MT` or `/MD` with `compileFlag(...)`, while Linux, macOS, Android, and iOS producers pass the options understood by their own compiler. CRT family is independent from whether the output itself is a static archive or a DLL; static libraries participating in the same final link must use compatible settings.

Android plugin configuration uses the build-tool enums directly: `androidApiLevel` is `Property<AndroidTarget.ApiLevel>`, `androidTargets` is `ListProperty<AndroidTarget.Target>`, and per-ABI hooks can use `androidTarget(AndroidTarget.Target.arm64_v8a) { ... }`. Keep the string overload only for compatibility with dynamic target names.

Path-like plugin DSL methods keep string properties for compatibility, but provide typed overloads for Gradle `Directory`, `RegularFile`, and provider values, plus Java `File` and `Path`. Prefer these overloads for project files, generated directories, headers, forced includes, native source files, linker inputs, and dependency IDL/module paths. Keep string values for globs, compiler flags, linker flags, Gradle task paths, and placeholders such as `{androidAbi}`. The shared target factory translates `forcedInclude(...)` to MSVC `/FI<header>` on `WindowsMSVCTarget` and GCC/Clang `-include<header>` on other targets.

`jParser_generate` composes build-runner switches from `JParserGenerationTarget` instead of raw `gen_jni`, `gen_ffm`, `gen_web`, and `gen_teavm_c` strings inside the plugin. The runner still receives the original string args at the boundary.

The repository root directly owns every `:jParser:*` and example project.
`gradle-plugin` is the only included build, selected through root
`pluginManagement`. The plugin build neither includes `jParser/` nor maps any
jParser source directory as one of its own projects. Every physical source
directory therefore has one Gradle owner, preventing IntelliJ from showing
multiple qualified module identities beside one folder.

The plugin implementation has no compile-time dependency on generator
implementation classes. When applied, it creates the resolvable
`jParserGeneratorClasspath` configuration. If
`:jParser:gen:gen-build-tool` exists in the applied project's root build, the
configuration uses that project directly; external consumers fall back to the
matching published `gen-build-tool` coordinate. Build tasks load the runner
from that classpath through an isolated request protocol. This is the same
local-project-or-published pattern used by the gdxTeavm plugin and allows a
clean checkout to change the generator, plugin, and examples without a Maven
publication.

The standalone plugin build imports the root version catalog only for normal
version and publication data. Its published plugin artifact does not embed the
generator implementation. Root Easy Publishing owns the jParser module
publications and coordinates the plugin build through its nested-build
lifecycle.

The TestLib and SharedLib desktop example Gradle files select their host target directly and depend on the matching generator task paths. Version and dependency data comes from `gradle/libs.versions.toml`; the root build does not use `buildSrc`.

Example generated output modules use this layout: `core`, `shared/<Lib>-jni`, `shared/<Lib>-c`, `desktop/<Lib>-desktop-ffm`, and `web/<Lib>-web`, with native packaging under `desktop/<Lib>-desktop-jni`, `desktop/<Lib>-desktop-c`, `android/<Lib>-android`, `android/<Lib>-android-c`, and `ios/<Lib>-ios-c`. The plugin fixtures keep their separate `plugin` modules, but set suffix overrides so generation targets this layout.

Runtime helper generation remains owned by `jParser/runtime/builder`, which
invokes `BuildRuntimeHelper` directly and exposes the
`runtime_helper_build_project*` tasks.

Shared-library examples use per-library plugin modules in `examples/SharedLib/libA/plugin` and `examples/SharedLib/libB/plugin`. These are normal root Gradle modules with only a `build.gradle.kts`; `libB` declares its `libA` module reference through `dependency("libA") { reference(...) }`, which expands IDL refs, header paths, native link inputs, and project task dependencies.

## JNI vs FFM

### JNI

- Java side: generated native method stubs.
- Native side: JNI ABI (`JNIEnv*`, `jlong`, `jint`, etc.).
- Parser/generator: `jParser/jParser-jni`.

### FFM

- Java side: `java.lang.foreign` downcalls via MethodHandles.
- Native side: C ABI (`extern "C"`, `int64_t`, `int32_t`, no `JNIEnv*`).
- Parser/generator: `jParser/jParser-ffm`.
- Build option is off by default (`BuildToolOptions.generateFFM=false`) unless enabled by task/config.

### TeaVM C

For public configuration and deployment guidance, see the [TeaVM C guide](teavm-c.md). This section describes generator and runtime internals.

- Java side: static native methods annotated with `org.teavm.interop.Import`.
- Native side: C ABI (`extern "C"`, `int64_t`, `int32_t`, no `JNIEnv*`).
- Parser/generator: `TeaVMCCodeParser` with `TeaVMCGenerator` in `jParser:gen:gen-c`.
- Build option is off by default (`BuildToolOptions.generateTeaVMC=false`) unless enabled with `gen_teavm_c`.
- Before generation, jParser clears the generated C Java output path so public-package files left by an older generator cannot survive an upgrade. New Java is written to `gen.c.<original-package>` below `BuildToolOptions.getCJavaOutputPath()`, not to the public package or the TeaVM web output path. Imports between generated binding classes are rewritten to the same `gen.c` namespace; native ABI symbol names continue to use the original public package.
- Native libraries are selected by platform target args such as `windows64_teavm_c`, `android_teavm_c`, or `ios_teavm_c`.
- IDL callback implementation glue is generated with TeaVM C imports/exports and C function pointers when callbacks are present.
- TeaVM C substitution is generic and classpath-driven: `emu.c.*` replacements win over `gen.c.*` replacements, and classes without a matching replacement remain unchanged.

TeaVM C native linkage is selected with the typed `TeaVMCLinkage` enum. `STATIC` is the compatibility default. `SHARED_LINKED` links the application against the generated shared library; the operating-system loader therefore resolves the symbols before application startup. `RUNTIME_LOADED` uses that shared library as a plugin while the application compiles a generated dispatch shim and the `loader-c` implementation. iOS accepts only `STATIC`; its native code is linked into the signed application. The public Java loader API remains `JParserLibraryLoader` in every mode. `JParserLibraryLoaderOptions.fileName` selects an exact physical payload name without automatic prefix or architecture-suffix decoration, and `path` selects its containing directory.

Every generated TeaVM C library has a versioned API-table description derived from its exported C symbols. The runtime-loaded path resolves one generated provider symbol, then validates the logical library name, ABI version, API-table size, symbol count, and generated fingerprint before publishing the table to the dispatch shim. A failed validation leaves the shim unbound. A successfully bound logical library cannot be rebound to another file, and the loader deliberately provides no unload operation: native objects, callbacks, and function pointers may remain live for the process lifetime.

Platform loading behavior is:

- Windows: `LoadLibraryExW` and `GetProcAddress`; `SHARED_LINKED` additionally needs the DLL import library when linking the application.
- Linux and macOS: `dlopen` with immediate, local symbol resolution and `dlsym`.
- Android: the same `dlopen`/`dlsym` path, with ABI-specific `.so` payloads and no desktop architecture suffix added to the logical library name.
- iOS: static linkage only, with ARM64 device and ARM64/x86_64 simulator archives selected at native application build time. Arbitrary downloaded plugins are outside the supported model.

Configuration is identical for the plugin and manual builder paths. The plugin uses `jParser { teaVMCLinkage.set(TeaVMCLinkage.RUNTIME_LOADED) }`; a manual builder assigns `BuildToolOptions.BuildToolParams.teaVMCLinkage` before constructing `BuildToolOptions`. The system-property runner also accepts `-Djparser.teaVMCLinkage=STATIC`, `SHARED_LINKED`, or `RUNTIME_LOADED` (case-insensitive).

TeaVM C generation also writes portable native-build resources under the configured C module's `build/generated/jparser/resources/main` directory. The C module must add that directory to its main resources and make `processResources` depend on the generation task. The resulting main C jar contains:

```text
META-INF/gdx-teavm.properties
external_cpp/cmake/post_target/jparser_<library>_teavm_c.cmake
external_cpp/jparser/<library>/
  glue/TeaVMCGlue.cpp
  glue/TeaVMCGlue.h
  imports/teavmc_imports.h
  teavmcabi/TeaVMCAbi.h
  teavmcabi/TeaVMCDispatch.h
  teavmcabi/TeaVMCDispatch.cpp
  teavmcabi/teavmc_abi.properties
  custom/**
  runtime/RuntimeHelper.h
  source/**
```

The separately published `loader-c` jar contributes the common resources that every mode needs:

```text
external_cpp/cmake/post_target/jparser_00_teavmc_loader.cmake
external_cpp/jparser/loader/teavmc_loader.h
external_cpp/jparser/loader/teavmc_loader.cpp
```

Generated binding jars add `resources=loader-c-` to their discovery marker, so gdx-teavm also extracts the versioned loader jar. `loader-c` deliberately has no second `META-INF/gdx-teavm.properties`: keeping a single marker avoids duplicate Java-resource failures when Android resolves `runtime-c` and its transitive loader dependency.

Native payload artifacts add the archive, shared binary, and, on Windows, import library below `external_cpp/jparser/<library>/native/<platform>`. A consumer must extract the binding C jar, `loader-c`, and the selected native payload into one filesystem resource tree before configuring CMake. A shared binary cannot remain inside a jar at runtime; it must also be deployed as a physical file that the operating-system loader can open.

Producer native hooks and final-consumer metadata are intentionally separate. Target-level `headerDir`, `compileFlag`, linker inputs, and related hooks configure the producer build and may contain local absolute paths. A TeaVM C target or variant may additionally declare a nested `consumer { ... }` block containing only resource-relative selectors, include directories, compile definitions/options, static archives, and static link libraries/options. The Gradle plugin collects every enabled TeaVM C consumer declaration in the build request; `TeaVMCPortableResourceWriter` embeds them in the binding's one standard generated `jparser_<library>_teavm_c.cmake` file. Native payload artifacts therefore package headers/libraries, not additional ordered CMake hooks.

For the current CMake platform, each generated consumer candidate checks every `selectorResource` below `native/<platform>`. Exactly one candidate must match. More than one matching artifact is rejected as ambiguous, while a configured platform with no matching artifact reports that the required native artifact is missing. Selected header directories and compile settings apply to the application target in every linkage mode. Declared packaged archives, system libraries, and linker options apply only when the binding linkage is `STATIC`; dynamic modes keep those implementation dependencies behind the binding shared library. A producer may give each packaged archive an optional CMake override variable. On Windows the generated candidate search first checks the consumer-selected `md` or `mt` resource subdirectory and then the platform root, without modifying `MSVC_RUNTIME_LIBRARY`.

Consumer metadata maps `windows64_teavm_c`, `linux64_teavm_c`, `mac64_teavm_c`, `macArm_teavm_c`, `android_teavm_c`, and `ios_teavm_c` to the portable resource layouts below. The iOS target always produces static archives and uses `xcrun` with the current Xcode SDK rather than embedding a fixed SDK path.

The CMake hooks are consumer-neutral. The loader hook adds the common loader implementation once, and each binding hook adds its descriptor/dispatch source. Before including the hooks, a native build can define these global overrides:

- `JPARSER_TEAVMC_APP_TARGET`: application or library target that receives the loader, generated sources, includes, and native link inputs. `TEAVM_APP_TARGET` remains its compatibility fallback.
- `JPARSER_TEAVMC_GENERATED_SOURCE_ROOT`: optional root containing TeaVM-generated `.c` sources. It defaults to `${CMAKE_CURRENT_SOURCE_DIR}/c/src`.
- `JPARSER_TEAVMC_LINKAGE`: optional `STATIC`, `SHARED_LINKED`, or `RUNTIME_LOADED` selection applied to every generated library hook that has no per-library override.
- `JPARSER_TEAVMC_RUNTIME_OUTPUT_DIRECTORY`: optional post-build destination for shared libraries. It defaults to the application target's output directory.

Each binding hook also accepts these per-library overrides, where `<LIBRARY>` is the upper-case CMake identifier generated from the logical library name:

- `JPARSER_<LIBRARY>_TEAVMC_ROOT`: optional extracted resource root for one library. By default it is resolved relative to the CMake hook.
- `JPARSER_<LIBRARY>_TEAVMC_LINKAGE`: optional linkage selection for this library. It wins over `JPARSER_TEAVMC_LINKAGE`; otherwise the hook uses the mode recorded when jParser generated it (`STATIC` by default).
- `JPARSER_<LIBRARY>_TEAVMC_LIBRARY`: explicit static archive for `STATIC`.
- `JPARSER_<LIBRARY>_TEAVMC_SHARED_LIBRARY`: explicit DLL, SO, or dylib for either dynamic mode.
- `JPARSER_<LIBRARY>_TEAVMC_IMPORT_LIBRARY`: explicit Windows import library for `SHARED_LINKED`.
- `JPARSER_<LIBRARY>_TEAVMC_RUNTIME_OUTPUT_DIRECTORY`: per-library shared-binary staging destination. It wins over the global runtime output directory.

Automatic payload selection uses these platform layouts:

| Platform | Resource platform | Static file | Shared file | Windows import file |
|---|---|---|---|---|
| Windows x64 | `windows_x64` | `<Lib>64_.lib` | `<Lib>64.dll` | `<Lib>64.lib` |
| Linux x64 | `linux_x64` | `lib<Lib>64_.a` | `lib<Lib>64.so` | -- |
| macOS x64 | `mac_x64` | `lib<Lib>64_.a` | `lib<Lib>64.dylib` | -- |
| macOS arm64 | `mac_arm64` | `lib<Lib>64_.a` | `lib<Lib>arm64.dylib` | -- |
| Android (portable resource layout) | `android/${ANDROID_ABI}` | `lib<Lib>.a` | `lib<Lib>.so` | -- |
| iOS device ARM64 | `ios/device/arm64` | `lib<Lib>64_.a` | -- | -- |
| iOS simulator ARM64 | `ios/simulator/arm64` | `lib<Lib>64_.a` | -- | -- |
| iOS simulator x86_64 | `ios/simulator/x86_64` | `lib<Lib>64_.a` | -- | -- |

For static archives, the hook checks `native/<platform>/<file>` and then `native/<platform>/static/<file>`. On Windows it first checks `native/windows_x64/<mt|md>/static/<file>`, using the target's standard `MSVC_RUNTIME_LIBRARY`, `CMAKE_MSVC_RUNTIME_LIBRARY`, or CMake's default MD family to select the directory. Dynamic payload lookup similarly prefers `native/windows_x64/<mt|md>/shared/<file>` before the generic `shared` and direct platform paths. jParser's current Android artifacts package shared libraries as AAR `jni/<abi>` entries rather than as portable `external_cpp` resources, so an Android CMake consumer must point `JPARSER_<LIBRARY>_TEAVMC_SHARED_LIBRARY` at its extracted/build-time `.so` and choose a staging destination that its application packaging includes. On iOS, the hook derives `device` or `simulator` from `CMAKE_OSX_SYSROOT`, requires one `CMAKE_OSX_ARCHITECTURES` value, selects the matching archive, creates an imported static target, and appends it to `TEAVM_IOS_STATIC_DEPENDENCY_TARGETS` when that consumer-owned list is defined.

`STATIC` compiles the glue into the application, links the archive, and forces the direct import header into TeaVM's generated C. `SHARED_LINKED` links the import/shared library, forces the direct import header, stages the shared binary, and adds `$ORIGIN` or `@loader_path` runtime search paths on Linux or macOS. `RUNTIME_LOADED` deliberately does not link the plugin; it forces the dispatch header, links the platform dynamic-loader support when required, and stages the plugin for `JParserLibraryLoader` to open. The hook requests `cxx_std_17` without lowering a newer consumer target. It does not force `/MT`, `/MD`, `_ITERATOR_DEBUG_LEVEL`, or any other platform policy. On MSVC it only inspects the existing standard runtime selection to choose a matching runtime-specific payload directory. Producers and consumers remain responsible for matching other ABI-affecting options such as debug iterators. jParser does not generate an iOS launcher, app bundle, `Info.plist`, Xcode project, or signing configuration; application frameworks and consumers own that layer.

gdx-teavm remains a compatibility adapter rather than a requirement of this layout. Its classpath discovery reads the generated `META-INF/gdx-teavm.properties`, extracts supported `external_cpp` resources, includes `external_cpp/cmake/post_target`, supplies `TEAVM_APP_TARGET` as the fallback application target, and uses the default generated-source root. The current gdx-teavm extractor accepts source, header, CMake, and static-library extensions but not `.dll`, `.so`, or `.dylib`; it must be extended before `SHARED_LINKED` or `RUNTIME_LOADED` can consume jParser's packaged shared payloads through that adapter. Other TeaVM C launchers must extract the same resources and shared binaries, set the portable variables they need, and include all generated post-target hooks directly.

## Native Comment Block Contract (`base`)

For author-facing examples, WebIDL behavior, and `IDLBase` ownership, see [Binding Authoring](binding-authoring.md).

Supported headers: `JNI`, `FFM`, `TEAVM`, `TEAVM_C`.

Supported commands: `-ADD`, `-ADD_RAW`, `-REMOVE`, `-REPLACE`, `-REPLACE_BLOCK`, `-NATIVE`.

Example:

```java
/*[-JNI;-NATIVE]
    MyType* obj = (MyType*)this_addr;
    obj->doSomething();
*/
/*[-FFM;-NATIVE]
    MyType* obj = (MyType*)this_addr;
    obj->doSomething();
*/
private static native void internal_native_doSomething(long this_addr);
```

`-NATIVE` attaches to the next Java `native` method declaration.

## Toolchain Notes

- JDK 11+ required for core build tooling.
- TeaVM 0.15+ web modules and compiler/tooling require JDK 17+.
- FFM tasks require newer JDK (22+; 25 preferred by project docs).
- Desktop FFM launcher tasks in examples use the `javaFfm` version from `gradle/libs.versions.toml`.
