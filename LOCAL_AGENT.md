# LOCAL_AGENT.md — Session State

## Current Task
FFM (Foreign Function & Memory API) — Phase 1 implementation complete.

## Current Progress
- [x] Deep codebase analysis: traced full JNI and TeaVM code generation pipelines
- [x] Created `FFM_PLAN.md` with 4-phase implementation plan
- [x] **Task 1.1**: Created `jParser/jParser-ffm` Gradle module (Java 11 build-time, generates Java 24 code)
- [x] **Task 1.1**: Registered in `settings.gradle.kts`, `publish.gradle.kts`, `jParser-build-tool/build.gradle.kts`
- [x] **Task 1.2**: Implemented `FFMCodeParser` extending `IDLDefaultCodeParser` with header `"FFM"`
- [x] **Task 1.3**: Implemented `FFMCppGenerator` — emits `extern "C"` functions with `int64_t`/`int32_t` types
- [x] **Task 1.4**: Implemented `FFMMethodHandleRegistry` — tracks MethodHandle entries per class
- [x] **Task 1.4**: Implemented `FFMTypeMapper` — maps Java types to ValueLayout/C types
- [x] Created `FFMNativeCodeGenerator` interface (decoupled from jParser-cpp CppGenerator)
- [x] Compilation verified: `jParser-ffm` and `jParser-build-tool` both compile successfully
- [x] Existing tests pass (jParser-idl:test)

## Next Task
Phase 2 implementation:
1. Extend `BuildToolOptions` with `generateFFM` flag
2. Extend `BuilderTool.generateAndBuild()` with FFM generation block
3. Add FFM build target helpers

## Files Created
- `jParser/jParser-ffm/build.gradle.kts`
- `jParser/jParser-ffm/src/main/java/com/github/xpenatan/jParser/ffm/FFMCodeParser.java`
- `jParser/jParser-ffm/src/main/java/com/github/xpenatan/jParser/ffm/FFMCppGenerator.java`
- `jParser/jParser-ffm/src/main/java/com/github/xpenatan/jParser/ffm/FFMMethodHandleRegistry.java`
- `jParser/jParser-ffm/src/main/java/com/github/xpenatan/jParser/ffm/FFMTypeMapper.java`
- `jParser/jParser-ffm/src/main/java/com/github/xpenatan/jParser/ffm/FFMNativeCodeGenerator.java`

## Files Modified
- `settings.gradle.kts` — Added `:jParser:jParser-ffm`
- `buildSrc/src/main/kotlin/publish.gradle.kts` — Added to `libProjects`
- `jParser/jParser-build-tool/build.gradle.kts` — Added dependency on `jParser-ffm`

## Key Design Decisions
- `jParser-ffm` module compiles on Java 11 (it's a build-time code generator); the **generated output** targets Java 24
- Created `FFMNativeCodeGenerator` interface instead of depending on `jParser-cpp`'s `CppGenerator` (decoupled)
- C++ templates use `int64_t`/`int32_t` casts instead of `jlong`/`jint`
- Symbol naming: `jparser_<package>_<class>_<method>__<overload>` 
- FFM bridge methods use `MethodHandle.invokeExact()` wrapped in try/catch
- `SymbolLookup.loaderLookup()` used for library resolution (relies on `System.loadLibrary`)
