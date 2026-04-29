# AGENTS.md - jParser

Token-light operational guide for coding agents in this repository.

## 1) Start Here (Mandatory)

Must do:

1. Read `LOCAL_AGENT.md`, then verify APIs/types in source (no guessing).
2. Record plan in `LOCAL_AGENT.md` and get user approval before execution.
3. After execution, update `LOCAL_AGENT.md` and run at least one compile/verification task.

If session resumes, `LOCAL_AGENT.md` is the source of truth.

Full protocol: `docs/agent-workflows.md`.

## 2) Project Snapshot

- jParser generates Java/native bindings for `JNI`, `FFM`, and `TeaVM` from `lib-base` + IDL.
- Pipeline entry point: `jParser/jParser-build-tool` (`BuilderTool.build()`).
- Generated outputs are not hand-edited:
  - `lib-core`
  - `lib-desktop-jni`
  - `lib-desktop-ffm`
  - `lib-android`
  - `lib-teavm`
- Key examples: `examples/TestLib`, `examples/SharedLib`.

## 3) JNI / FFM Essentials

- `JNI` generation: `CppCodeParser` + JNI C++ glue (`jlong`, `jint`, `JNIEnv*`).
- `FFM` generation: `FFMCodeParser` + C ABI glue (`extern "C"`, `int64_t`, `int32_t`) with Java MethodHandle downcalls.
- `BuildToolOptions.generateCPP` default `true`; `generateFFM` default `false`.
- FFM runtime/apps require modern JDK (22+; 25 preferred in project docs).
- Desktop FFM launcher tasks in examples are currently configured with Java toolchain 24 in their Gradle scripts.

## 4) Native Block Convention (`lib-base`)

- Headers: `JNI`, `FFM`, `TEAVM`.
- Commands: `-ADD`, `-ADD_RAW`, `-REMOVE`, `-REPLACE`, `-REPLACE_BLOCK`, `-NATIVE`.
- Use `-IDL_SKIP` on class comment to skip IDL generation.
- `-NATIVE` block binds to the following Java `native` method.

## 5) Quick Build/Test Path (Windows)

Run from repo root with `gradlew.bat`.

1. Build IDL runtime native artifacts (JNI + FFM):
   - `:idl:runtime:runtime-build:idl_helper_build_project_windows64_jni`
   - `:idl:runtime:runtime-build:idl_helper_build_project_windows64_ffm`
2. Build TestLib native artifacts:
   - `:examples:TestLib:lib:lib-build:TestLib_build_project_windows64_jni`
   - `:examples:TestLib:lib:lib-build:TestLib_build_project_windows64_ffm`
3. Verify desktop tests:
   - `:examples:TestLib:app:desktop-jni:test`
   - `:examples:TestLib:app:desktop-ffm:test`

Use platform variants (`linux64`, `mac64`, `macArm`) where needed.

## 6) Where Detailed Reference Lives

- `docs/agent-workflows.md` - persistence workflow, editing rules, handoff checklist.
- `docs/agent-architecture.md` - pipeline, module map, JNI/FFM/TeaVM internals.
- `docs/agent-commands.md` - full build/run/benchmark command matrix.

Keep this file concise. Put verbose command tables and background detail in `docs/`.
