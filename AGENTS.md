# AGENTS.md - jParser

Token-light operational guide for coding agents in this repository.

These instructions apply to the whole repository. `AGENTS.md` is the process
and coordination layer; durable architecture, command, and workflow facts live
in `docs/`.

## 1) Start Here (Mandatory)

Must do in this order:

1. Read `.agents/agents_memory.md` first. If it is absent, continue with no recovery state.
2. Validate the request against real source/APIs/types (no guessing).
3. Record the plan + target files in `.agents/agents_memory.md`; for multi-step work, also create/update `.agents/agents_plan.md`. Explain what you are going to do, answer questions, or give a recommendation.
4. Get user approval before execution.
5. Execute edits/commands.
6. Update `.agents/agents_memory.md` to current state only, and clear/update `.agents/agents_plan.md` when the active multi-step work changes or completes.
7. Run at least one compile/verification task before ending turn.

`.agents/agents_memory.md` is rolling state, not a running log: replace/update prior entries instead of appending history.

Execution details and checklists: `docs/workflows.md`.

## 2) Temporary Agent State

- `.agents/agents_memory.md`: local rolling state for the active request, status, last completed step, next step, blockers, and validation result.
- `.agents/agents_plan.md`: local recovery plan for multi-step tasks. Include target files/modules/tasks, why each step matters, intended action, dependencies/blockers, and acceptance evidence.
- Keep `.agents/*` local and ignored by Git. These files are scratch recovery state, not project source, history, architecture, or API contract.

## 3) Source of Truth

- `AGENTS.md`: process rules only.
- `docs/workflows.md`: editing workflow, verification minimum, and handoff checklist.
- `docs/architecture.md`: pipeline, module map, JNI/FFM/TeaVM internals.
- `docs/commands.md`: build/run/benchmark command matrix.
- `docs/getting-started.md`: user-facing project layout and first builds.
- `docs/binding-authoring.md`: native directives, WebIDL, and ownership reference.
- `docs/teavm-c.md`: TeaVM C linkage, packaging, and consumer configuration.
- `settings.gradle.kts`: root module source of truth.

When architecture, module names, tasks, generated-output rules, or public commands change, update the canonical docs and user-facing references in the same change.

## 4) Project Snapshot

- jParser generates Java/native bindings for `JNI`, `FFM`, TeaVM web, and TeaVM C from handwritten base modules + IDL.
- Pipeline entry point: `jParser/jParser-build-tool` (`BuilderTool.build()`).
- Do not hand-edit generated outputs; edit source modules and generators.
- Root module source of truth: `settings.gradle.kts`.
- Key examples: `examples/TestLib`, `examples/SharedLib`.

Maintenance rule: when module/task names change, update `AGENTS.md` and related docs in the same change to keep names aligned with `settings.gradle.kts` and Gradle task definitions.

## 5) Current Module Naming (Examples)

- Example library pipeline folders: `base`, `builder`, `core`, `shared/<Lib>-jni`, `shared/<Lib>-c`, `desktop/<Lib>-desktop-jni`, `desktop/<Lib>-desktop-ffm`, `desktop/<Lib>-desktop-c`, `web/<Lib>-web`, `android/<Lib>-android`, `android/<Lib>-android-c`, and `ios/<Lib>-ios-c`.
- Runtime folders: `runtime/base`, `runtime/builder`, `runtime/core`, `runtime/shared/runtime-jni`, `runtime/shared/runtime-c`, `runtime/desktop/runtime-desktop-jni`, `runtime/desktop/runtime-desktop-ffm`, `runtime/desktop/runtime-desktop-c`, `runtime/web/runtime-web`, `runtime/android/runtime-android`, `runtime/android/runtime-android-c`, and `runtime/ios/runtime-ios-c`.
- Split Gradle project paths use the same artifact-style leaf names as the folders, for example `:jParser:runtime:shared:runtime-jni`, `:jParser:runtime:desktop:runtime-desktop-jni`, `:jParser:runtime:desktop:runtime-desktop-ffm`, `:jParser:runtime:web:runtime-web`, `:jParser:runtime:android:runtime-android`, `:jParser:runtime:shared:runtime-c`, `:jParser:runtime:desktop:runtime-desktop-c`, `:jParser:runtime:android:runtime-android-c`, and `:jParser:runtime:ios:runtime-ios-c`.
- Example split Gradle paths follow the same pattern, such as `:examples:TestLib:lib:shared:TestLib-jni`, `:examples:TestLib:lib:shared:TestLib-c`, `:examples:TestLib:lib:desktop:TestLib-desktop-jni`, `:examples:TestLib:lib:desktop:TestLib-desktop-ffm`, `:examples:TestLib:lib:desktop:TestLib-desktop-c`, `:examples:TestLib:lib:web:TestLib-web`, `:examples:TestLib:lib:android:TestLib-android`, `:examples:TestLib:lib:android:TestLib-android-c`, and `:examples:TestLib:lib:ios:TestLib-ios-c`.
- App platform modules in examples: `app:platforms:desktop-jni`, `app:platforms:desktop-ffm`, `app:platforms:desktop-c`, `app:platforms:web`, `app:platforms:android`, `app:platforms:android-c`, `app:platforms:ios-c`.
- Runtime generator module: `jParser:runtime:builder` (tasks like `runtime_helper_build_project_<target>`).
- Gradle plugin included build: `jParser/tools/gradle-plugin` (plugin id `com.github.xpenatan.jparser`, artifact `jparser-gradle-plugin`).
- TeaVM C loader modules: `jParser/loader/loader-core` provides the shared Java loader API, while `jParser/loader/loader-c` provides its `emu.c` substitution plus the portable C/C++ loader ABI and implementation.
- Gradle plugin validation fixtures: `jParser/runtime/plugin`, `examples/TestLib/lib/plugin`, `examples/SharedLib/libA/plugin`, and `examples/SharedLib/libB/plugin`.
- Benchmark module namespace: `jParser:benchmark:benchmark-core`.

## 6) JNI / FFM Essentials

- `JNI` generation: `CppCodeParser` + JNI C++ glue (`jlong`, `jint`, `JNIEnv*`).
- `FFM` generation: `FFMCodeParser` + C ABI glue (`extern "C"`, `int64_t`, `int32_t`) with Java MethodHandle downcalls.
- `BuildToolOptions.generateCPP` default `true`; `generateFFM` default `false`.
- TeaVM C linkage uses `TeaVMCLinkage`: `STATIC` is the default, with `SHARED_LINKED` and `RUNTIME_LOADED` available for external native libraries. Runtime-loaded libraries bind once and stay loaded for the process lifetime.
- Detailed toolchain/platform notes live in `docs/architecture.md` and `docs/commands.md`.

## 7) Native Block Convention (`base`)

- Headers: `JNI`, `FFM`, `TEAVM`, `TEAVM_C`.
- Commands: `-ADD`, `-ADD_RAW`, `-REMOVE`, `-REPLACE`, `-REPLACE_BLOCK`, `-NATIVE`.
- Use `-IDL_SKIP` on class comment to skip IDL generation.
- `-NATIVE` block binds to the following Java `native` method.

## 8) Quick Build/Test Path (Windows)

Canonical command matrices live in `docs/commands.md`.

## 9) Where Detailed Reference Lives

- `docs/workflows.md` - editing rules, verification minimum, handoff checklist.
- `docs/architecture.md` - pipeline, module map, JNI/FFM/TeaVM internals.
- `docs/commands.md` - full build/run/benchmark command matrix.
- `docs/getting-started.md` - project layout and first TestLib builds.
- `docs/binding-authoring.md` - directive, WebIDL, and ownership guide.
- `docs/teavm-c.md` - native TeaVM C linkage and packaging guide.

Keep this file concise. Put verbose command tables and background detail in `docs/`.
