# Agent Architecture Reference

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
- `core`: generated bridge-agnostic API in examples
- `shared/<Lib>-jni`: generated JNI Java shared by desktop and Android JNI examples
- `shared/<Lib>-c`: generated TeaVM C Java shared by desktop and Android C examples
- `desktop/<Lib>-desktop-jni`: desktop JNI native packaging with a dependency on `shared/<Lib>-jni`
- `desktop/<Lib>-desktop-ffm`: generated FFM Java + desktop FFM natives
- `desktop/<Lib>-desktop-c`: TeaVM C desktop native payloads
- `android/<Lib>-android`: Android JNI packaging with a dependency on `shared/<Lib>-jni`
- `android/<Lib>-android-c`: Android TeaVM C native packaging
- `web/<Lib>-web`: generated TeaVM web output

Runtime modules mirror the example and binding layout:

- `runtime/base`: handwritten runtime helper source published as `runtime-base`.
- `runtime/builder`: generator and native build driver.
- `runtime/core`: public/shared runtime API published as `runtime-core`.
- `runtime/shared/runtime-jni`: generated JNI Java shared by desktop and Android, published as Java-only `runtime-jni`.
- `runtime/shared/runtime-c`: generated TeaVM C Java classes published as `runtime-c`.
- `runtime/desktop/runtime-desktop-jni`: desktop JNI packaging published as `runtime-desktop-jni` with a dependency on `runtime-jni`, plus native-only split artifacts such as `runtime-desktop-jni_windows_x64`.
- `runtime/desktop/runtime-desktop-ffm`: generated FFM Java and desktop FFM native payloads, published as `runtime-desktop-ffm`.
- `runtime/desktop/runtime-desktop-c`: desktop TeaVM C native-only split artifacts such as `runtime-desktop-c_windows_x64`.
- `runtime/web/runtime-web`: generated TeaVM web output and WebAssembly payloads, published as `runtime-web`.
- `runtime/android/runtime-android`: Android JNI packaging published as `runtime-android` with a dependency on `runtime-jni`, plus ABI payload artifacts such as `runtime-android_arm64_v8a`.
- `runtime/android/runtime-android-c`: Android TeaVM C packaging published as `runtime-android-c` plus ABI payload artifacts such as `runtime-android-c_arm64_v8a`.

Split runtime Gradle project paths use the same artifact-style leaf names as the folders. Example split modules follow the same pattern, such as `:examples:TestLib:lib:shared:TestLib-jni` at `examples/TestLib/lib/shared/TestLib-jni` and `:examples:TestLib:lib:desktop:TestLib-desktop-jni` at `examples/TestLib/lib/desktop/TestLib-desktop-jni`.

`runtime-web` owns jParser's TeaVM web substitution service. Binding web modules should depend on `runtime-web`; the runtime policy maps any class to `emu.web.<original-class>` or `gen.web.<original-class>` only when that replacement class is present on the TeaVM classpath. The `emu.web` rule is evaluated before `gen.web`, so explicit emulation wins over generated substitutions. `loader-web` contains the web loader implementation classes but does not register a TeaVM substitution service itself.

`runtime/shared/runtime-c` owns jParser's TeaVM C substitution service. Binding C modules should depend on `runtime-c`; the runtime policy maps any class to `emu.c.<original-class>` or `gen.c.<original-class>` only when that replacement class is present on the TeaVM classpath. The `emu.c` rule is evaluated before `gen.c`, so explicit emulation wins over generated substitutions.

Example app modules in examples use:

- `app:core` for shared app code.
- `app:assets` where an example has shared assets.
- `app:platforms:desktop-jni`
- `app:platforms:desktop-ffm`
- `app:platforms:desktop-c`
- `app:platforms:web`
- `app:platforms:android`
- `app:platforms:android-c`

Pattern repeats across `examples/`, `idl/`, `loader/`, and `jParser/` modules (see `settings.gradle.kts`).

## Gradle Plugin Support

`jParser/tools/gradle-plugin` is an included build that publishes the Maven artifact `com.github.xpenatan.jParser:jparser-gradle-plugin` and plugin id `com.github.xpenatan.jparser`.

The plugin is scoped to build-module orchestration: it creates one task namespace with `jParser_generate` and platform build tasks such as `jParser_build_windows64_jni`. The tasks reuse `BuilderTool`, `BuildToolOptions`, `BuildMultiTarget`, and the platform target classes through `JParserBuildRunner` and `DefaultBuildTargetFactory` in `jParser:gen:gen-build-tool`. Directly invoking `jParser_generate` generates core plus every configured binding family: JNI, FFM, TeaVM web, and TeaVM C. Each regular or variant `jParser_build_*` task generates core plus exactly the binding family required by its platform target and does not invoke the aggregate `jParser_generate` task. Native build task registration follows explicit `native { target(...) }` and `native { targetVariant(...) }` declarations when any are present; module suffixes are only used as the fallback for builds with no explicit native target list.

Symbol naming is configured with the typed build-tool enum `JParserSymbolNameMode` (`DEFAULT` or `OBFUSCATED`) for `jniSymbolNameMode`, `ffmSymbolNameMode`, and `teaVMCSymbolNameMode`; plugin build scripts must not set these values with raw strings.

IDL method renaming is configured by passing an `IDLRenaming` callback to the plugin with `idlRenaming(...)`. This is the same hook used by manual `BuilderTool.build` calls, so builds can rename methods, enums, or packages without rewriting source IDL files. `IDLMethod.name` remains the original IDL/native method name; `IDLMethod.bindingName` stores the generated Java/API method name after explicit IDL `Rename`, overload suffix cleanup, and `IDLRenaming`. Web `@JSBody` native-call generation must call the original exported JavaScript method name from `IDLMethod.name`, not the renamed Java/API binding name.

Native source selection is controlled by `JParserTargetHooks` in `JParserExtension.kt`. By default, each target compiles the parsed source tree with a recursive `**.cpp` glob and the build module's `src/main/cpp/custom/*.cpp` helper sources. Set `includeDefaultSources` or `includeCustomSources` to `false` when a target should use only explicit `cppInclude(...)` globs, for example when binding a C library, linking prebuilt artifacts, or avoiding platform-only helper files.

Native platform targets are selected with the typed `JParserTargets` enum, for example `target(JParserTargets.WINDOWS64_JNI) { ... }`. The public DSL does not accept raw platform target strings, so misspelled target names fail at build-script compilation time. Native target variants are configured with `targetVariant(JParserTargets.WINDOWS64_JNI, variantName) { ... }`. The plugin registers tasks named `jParser_build_<targetName>_<variantName>` and passes the original target-name string to the build tool, so generation and platform selection stay the same while include/link hooks can differ per variant. When a target has one or more variants, the unsuffixed root task `jParser_build_<targetName>` is not registered, even if base target hooks are also configured for that target. Variants default `outputDirectoryPrefix` to the variant name and write native outputs under `build/c++/libs/<variant>/...` and intermediates under `build/c++/target/<variant>/...`. This is for mutually exclusive backends of the same platform target, such as `windows64_jni_wgpu` and `windows64_jni_dawn`. By default variant hooks do not inherit the base target hooks; set `includeBaseTargetHooks` to `true` only when the variant should extend the base target instead of replacing its include/link inputs. Android variants expose the same typed and string `androidTarget(...)` ABI hooks as regular Android targets. Their ABI hooks replace base ABI hooks by default, or are applied after base ABI hooks when `includeBaseTargetHooks` is enabled.

Android plugin configuration uses the build-tool enums directly: `androidApiLevel` is `Property<AndroidTarget.ApiLevel>`, `androidTargets` is `ListProperty<AndroidTarget.Target>`, and per-ABI hooks can use `androidTarget(AndroidTarget.Target.arm64_v8a) { ... }`. Keep the string overload only for compatibility with dynamic target names.

Path-like plugin DSL methods keep string properties for compatibility, but provide typed overloads for Gradle `Directory`, `RegularFile`, and provider values, plus Java `File` and `Path`. Prefer these overloads for project files, generated directories, headers, forced includes, native source files, linker inputs, and dependency IDL/module paths. Keep string values for globs, compiler flags, linker flags, Gradle task paths, and placeholders such as `{androidAbi}`.

`jParser_generate` composes build-runner switches from `JParserGenerationTarget` instead of raw `gen_jni`, `gen_ffm`, `gen_web`, and `gen_teavm_c` strings inside the plugin. The runner still receives the original string args at the boundary.

The plugin included build follows the libfdx layout: it is not included as a root subproject, and its `settings.gradle.kts` must not include or remap root `:jParser:*` projects. It also must not rename the root project to the Maven artifact id; leave the included build name as the folder-derived `gradle-plugin`, and keep artifact naming in `build.gradle.kts`. Plugin code depends on the jParser generator/build artifacts instead of sourcing their classes into the plugin jar. `jParser/tools/gradle-plugin/buildSrc` sources the single root `buildSrc/src/main/kotlin/LibExt.kt` file for build-script constants. Do not add another `LibExt.kt` under `jParser/tools`; root `LibExt` is the only source of truth.

Example generated output modules use the jBox3D-style layout: `core`, `shared/<Lib>-jni`, `shared/<Lib>-c`, `desktop/<Lib>-desktop-ffm`, and `web/<Lib>-web`, with native packaging under `desktop/<Lib>-desktop-jni`, `desktop/<Lib>-desktop-c`, `android/<Lib>-android`, and `android/<Lib>-android-c`. The plugin fixtures keep their separate `plugin` modules, but set suffix overrides so generation targets this layout.

Runtime helper generation uses the same plugin in `jParser/runtime/plugin` with `runtimeHelper()` enabled. This module sits next to `runtime/builder`, has only a `build.gradle.kts`, and keeps the runtime tree from introducing one-off wrapper folders. That mode keeps `idlName` and `cppSourcePath` optional, generates the runtime helper sources, compiles `RuntimeHelper.cpp`, and switches the web target to the existing Emscripten main-module defaults.

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

- Java side: static native methods annotated with `org.teavm.interop.Import`.
- Native side: C ABI (`extern "C"`, `int64_t`, `int32_t`, no `JNIEnv*`).
- Parser/generator: `TeaVMCCodeParser` with `TeaVMCGenerator` in `jParser:gen:gen-c`.
- Build option is off by default (`BuildToolOptions.generateTeaVMC=false`) unless enabled with `gen_teavm_c`.
- Generated Java is written to the C output path (`BuildToolOptions.getCJavaOutputPath()`), not the TeaVM web output path.
- Native libraries are selected by platform target args such as `windows64_teavm_c`, `android_teavm_c`, or `ios_teavm_c`.
- IDL callback implementation glue is generated with TeaVM C imports/exports and C function pointers when callbacks are present.
- When TeaVM C generation runs, the C core artifact also receives gdx-teavm classpath resources: a `META-INF/gdx-teavm.properties` marker, `external_cpp/cmake/post_target` CMake hook, import prototypes, generated glue, copied custom sources, runtime helper header, and header-only source includes. Platform modules still package the matching static native libraries under `external_cpp/jparser/<lib>/native/<platform>`.
- TeaVM C substitution is generic and classpath-driven: `emu.c.*` replacements win over `gen.c.*` replacements, and classes without a matching replacement remain unchanged.

## Native Comment Block Contract (`base`)

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
- Desktop FFM launcher tasks in examples use `LibExt.javaFFMTarget` from Gradle configuration.
