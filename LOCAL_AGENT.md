# LOCAL_AGENT.md — Session State

## Current Task
FFM (Foreign Function & Memory API) — Phase 3: Runtime & Downstream Module Convention — **COMPLETE**

## Current Progress
- [x] Phase 1 complete (all tasks 1.1–1.4)
- [x] Phase 2 complete (all tasks 2.1–2.3)
- [x] **Task 3.1**: Decision on `loader-ffm` — skipped (SymbolLookup.loaderLookup() works with loader-core)
- [x] **Task 3.2**: Created TestLib `lib-ffm` module (generated FFM Java destination)
- [x] **Task 3.3**: Updated TestLib `BuildLib.java` with FFM arg handling + 4 FFM platform build target methods
- [x] **Task 3.4**: Added 5 FFM Gradle tasks to TestLib `lib-build/build.gradle.kts`
- [x] Compilation verified: `lib-ffm`, `lib-build` both BUILD SUCCESSFUL
- [x] Existing tests pass (`jParser-idl:test`)

## Next Task
Phase 4 (Advanced — deferred per plan):
- Task 4.1: Callback support via `upcallStub`
- Task 4.2: Array/Buffer optimization
- Task 4.3: `[-FFM;-NATIVE]` code block support

## Files Created (Phase 3)
- `examples/TestLib/lib/lib-ffm/build.gradle.kts` — FFM generated output module (mirrors lib-core)

## Files Modified (Phase 3)
- `settings.gradle.kts` — Added `include(":examples:TestLib:lib:lib-ffm")`
- `examples/TestLib/lib/lib-build/src/main/java/BuildLib.java` — Added FFM arg handling + 4 FFM target methods
- `examples/TestLib/lib/lib-build/build.gradle.kts` — Added 5 FFM Gradle tasks

## Key Design Decisions (Phase 3)
- **No loader-ffm needed**: `SymbolLookup.loaderLookup()` already works with `loader-core`'s `System.load()`
- **FFM targets output to `<platform>/ffm/`**: avoids conflict with JNI `.dll`/`.so`/`.dylib` files
- **Separate arg namespace**: `ffm_windows64` vs `windows64` keeps JNI and FFM builds independent
- **lib-ffm targets Java 11 for now**: Structure is ready; requires Java 24 to compile generated code

## Full File Change Summary (all phases)

### Phase 1 — Created
- `jParser/jParser-ffm/build.gradle.kts`
- `jParser/jParser-ffm/src/main/java/com/github/xpenatan/jParser/ffm/FFMCodeParser.java`
- `jParser/jParser-ffm/src/main/java/com/github/xpenatan/jParser/ffm/FFMCppGenerator.java`
- `jParser/jParser-ffm/src/main/java/com/github/xpenatan/jParser/ffm/FFMMethodHandleRegistry.java`
- `jParser/jParser-ffm/src/main/java/com/github/xpenatan/jParser/ffm/FFMTypeMapper.java`
- `jParser/jParser-ffm/src/main/java/com/github/xpenatan/jParser/ffm/FFMNativeCodeGenerator.java`

### Phase 1 — Modified
- `settings.gradle.kts` — Added `:jParser:jParser-ffm`
- `buildSrc/src/main/kotlin/publish.gradle.kts` — Added to `libProjects`
- `jParser/jParser-build-tool/build.gradle.kts` — Added dependency on `jParser-ffm`

### Phase 2 — Modified
- `jParser/jParser-build/src/main/java/.../tool/BuildToolOptions.java` — Added `generateFFM`, `moduleFFMPath`, `getModuleFFMPath()`
- `jParser/jParser-build-tool/src/main/java/.../tool/BuilderTool.java` — Added FFM generation block
- `jParser/jParser-build/src/main/java/.../DefaultBuildTarget.java` — Added `addFFMGlueCode()` method

### Phase 3 — Created
- `examples/TestLib/lib/lib-ffm/build.gradle.kts`

### Phase 3 — Modified
- `settings.gradle.kts` — Added `:examples:TestLib:lib:lib-ffm`
- `examples/TestLib/lib/lib-build/src/main/java/BuildLib.java` — FFM arg handling + FFM targets
- `examples/TestLib/lib/lib-build/build.gradle.kts` — FFM Gradle tasks
