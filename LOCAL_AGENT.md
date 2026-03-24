# LOCAL_AGENT.md — Session State

## Current Task
FFM (Foreign Function & Memory API) — Phase 4: Advanced Features — **COMPLETE**

## Current Progress
- [x] Phase 1 complete (all tasks 1.1–1.4)
- [x] Phase 2 complete (all tasks 2.1–2.3)
- [x] Phase 3 complete (all tasks 3.1–3.4)
- [x] **Task 4.1**: Callback support via `upcallStub` in `FFMCodeParser`
- [x] **Task 4.2**: Array/Buffer optimization helpers in `FFMTypeMapper`
- [x] **Task 4.3**: `[-FFM;-NATIVE]` code block support — verified working via existing pipeline
- [x] Compilation verified: `jParser-ffm`, `jParser-build-tool` both BUILD SUCCESSFUL
- [x] Existing tests pass (`jParser-idl:test`)

## All Phases Complete

All 4 phases of the FFM implementation plan are now complete:
- **Phase 1**: Core FFM module & code parser (`FFMCodeParser`, `FFMCppGenerator`, `FFMMethodHandleRegistry`, `FFMTypeMapper`)
- **Phase 2**: Build integration (`BuildToolOptions.generateFFM`, `BuilderTool` FFM block, `DefaultBuildTarget.addFFMGlueCode()`)
- **Phase 3**: Runtime & downstream (`TestLib lib-ffm` module, `BuildLib` FFM targets, Gradle tasks)
- **Phase 4**: Advanced features (callback via `upcallStub`, array/buffer helpers, `[-FFM;-NATIVE]` blocks)

## Files Modified (Phase 4)
- `jParser/jParser-ffm/src/main/java/.../FFMCodeParser.java` — Added `onIDLCallbackGenerated()` + 7 helper methods for C++ callback class generation and upcall stub creation
- `jParser/jParser-ffm/src/main/java/.../FFMCppGenerator.java` — Added `addCallbackClassCode()` method
- `jParser/jParser-ffm/src/main/java/.../FFMNativeCodeGenerator.java` — Added `addCallbackClassCode()` to interface
- `jParser/jParser-ffm/src/main/java/.../FFMTypeMapper.java` — Added array type mappings + `getArraySegmentCode()`, `getBufferSegmentCode()`, `getArrayElementLayout()`, `isArrayType()` helpers

## Key Design Decisions (Phase 4)
- **Function pointers instead of JNI callbacks**: C++ callback class stores typed function pointers instead of JNIEnv*/jobject/jmethodID. Virtual methods call function pointers directly.
- **upcallStub with Arena.ofAuto()**: GC-managed arena means stubs are freed when the Java callback is garbage-collected. No manual lifecycle management.
- **Static native setupCallback**: FFM uses explicit `this_addr` param (static method), unlike JNI which uses non-static native with implicit env/obj.
- **Array types map to ValueLayout.ADDRESS**: Arrays are passed as MemorySegment pointers, with C types like `int32_t*`, `float*`, etc.
- **[-FFM;-NATIVE] already works**: The existing pipeline (DefaultCodeParser.parseCodeBlock → CMD_NATIVE → setJavaBodyNativeCMD) handles it correctly for native methods with the FFM header.
