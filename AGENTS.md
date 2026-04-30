# AGENTS.md - jParser

Token-light operational guide for coding agents in this repository.

## 1) Start Here (Mandatory)

Must do in this order:

1. Read `LOCAL_AGENT.md` first.
2. Validate the request against real source/APIs/types (no guessing).
3. Record plan + target files in `LOCAL_AGENT.md`.
4. Get user approval before execution.
5. Execute edits/commands.
6. Update `LOCAL_AGENT.md` to current state only.
7. Run at least one compile/verification task before ending turn.

`LOCAL_AGENT.md` is rolling state, not a running log: replace/update prior entries instead of appending history.

Execution details and checklists: `docs/workflows.md`.

## 2) Project Snapshot

- jParser generates Java/native bindings for `JNI`, `FFM`, and TeaVM web from `lib-base` + IDL.
- Pipeline entry point: `jParser/jParser-build-tool` (`BuilderTool.build()`).
- Do not hand-edit generated outputs; edit source modules and generators.
- Key examples: `examples/TestLib`, `examples/SharedLib`.

Maintenance rule: when module/task names change, update `AGENTS.md` and related docs in the same change to keep names aligned with the source of truth (`settings.gradle.kts` and Gradle task definitions).

## 3) JNI / FFM Essentials

- `JNI` generation: `CppCodeParser` + JNI C++ glue (`jlong`, `jint`, `JNIEnv*`).
- `FFM` generation: `FFMCodeParser` + C ABI glue (`extern "C"`, `int64_t`, `int32_t`) with Java MethodHandle downcalls.
- `BuildToolOptions.generateCPP` default `true`; `generateFFM` default `false`.
- Detailed toolchain/platform notes live in `docs/architecture.md` and `docs/commands.md`.

## 4) Native Block Convention (`lib-base`)

- Headers: `JNI`, `FFM`, `TEAVM`.
- Commands: `-ADD`, `-ADD_RAW`, `-REMOVE`, `-REPLACE`, `-REPLACE_BLOCK`, `-NATIVE`.
- Use `-IDL_SKIP` on class comment to skip IDL generation.
- `-NATIVE` block binds to the following Java `native` method.

## 5) Quick Build/Test Path (Windows)

Canonical command matrices live in `docs/commands.md`.

## 6) Where Detailed Reference Lives

- `docs/workflows.md` - editing rules, verification minimum, handoff checklist.
- `docs/architecture.md` - pipeline, module map, JNI/FFM/TeaVM internals.
- `docs/commands.md` - full build/run/benchmark command matrix.

Keep this file concise. Put verbose command tables and background detail in `docs/`.
