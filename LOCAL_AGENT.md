# LOCAL_AGENT.md — Session State

## Current Task
FFM (Foreign Function & Memory API) — Phase 2: Build Integration — **COMPLETE**

## Current Progress
- [x] Phase 1 complete (all tasks 1.1–1.4)
- [x] **Task 2.1**: Extend `BuildToolOptions` with `generateFFM` flag and `moduleFFMPath`
- [x] **Task 2.2**: Extend `BuilderTool.generateAndBuild()` with FFM generation block
- [x] **Task 2.3**: Add `addFFMGlueCode()` helper in `DefaultBuildTarget`
- [x] Compilation verified: `jParser-build`, `jParser-build-tool`, `jParser-ffm` all BUILD SUCCESSFUL
- [x] Existing tests pass (`jParser-idl:test`)

## Next Task
Phase 3 implementation:
1. Create `loader/loader-ffm` module
2. Establish `lib-ffm` module convention
3. Update TestLib example

## Files Modified (Phase 2)
- `jParser/jParser-build/src/main/java/.../tool/BuildToolOptions.java` — Added `generateFFM`, `moduleFFMPath`, `getModuleFFMPath()`
- `jParser/jParser-build-tool/src/main/java/.../tool/BuilderTool.java` — Added FFM generation block with `FFMCodeParser` + `FFMCppGenerator`
- `jParser/jParser-build/src/main/java/.../DefaultBuildTarget.java` — Added `addFFMGlueCode()` method

## Files Created (Phase 1)
- `jParser/jParser-ffm/build.gradle.kts`
- `jParser/jParser-ffm/src/main/java/com/github/xpenatan/jParser/ffm/FFMCodeParser.java`
- `jParser/jParser-ffm/src/main/java/com/github/xpenatan/jParser/ffm/FFMCppGenerator.java`
- `jParser/jParser-ffm/src/main/java/com/github/xpenatan/jParser/ffm/FFMMethodHandleRegistry.java`
- `jParser/jParser-ffm/src/main/java/com/github/xpenatan/jParser/ffm/FFMTypeMapper.java`
- `jParser/jParser-ffm/src/main/java/com/github/xpenatan/jParser/ffm/FFMNativeCodeGenerator.java`

## Files Modified (Phase 1)
- `settings.gradle.kts` — Added `:jParser:jParser-ffm`
- `buildSrc/src/main/kotlin/publish.gradle.kts` — Added to `libProjects`
- `jParser/jParser-build-tool/build.gradle.kts` — Added dependency on `jParser-ffm`

## Key Design Decisions
- `jParser-ffm` module compiles on Java 11 (build-time code generator); generated output targets Java 24
- Created `FFMNativeCodeGenerator` interface instead of depending on `jParser-cpp`'s `CppGenerator`
- C++ templates use `int64_t`/`int32_t` casts instead of `jlong`/`jint`
- Symbol naming: `jparser_<package>_<class>_<method>__<overload>`
- FFM bridge methods use `MethodHandle.invokeExact()` wrapped in try/catch
- `SymbolLookup.loaderLookup()` used for library resolution
