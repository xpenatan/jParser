# Agent Workflows

## Session Protocol

1. Read `LOCAL_AGENT.md` first.
2. Validate the request against real source files (no guessing).
3. Write plan + files to be modified in `LOCAL_AGENT.md`.
4. Ask user approval before execution.
5. Execute edits/commands.
6. Update `LOCAL_AGENT.md` progress and next task.
7. Run compile/verification before ending turn.

## Editing Rules

- Do not hand-edit generated outputs in `lib-core`, `lib-desktop-jni`, `lib-desktop-ffm`, `lib-android`, `lib-teavm`.
- Prefer edits in source modules (`lib-base`, parser modules, build modules).
- If generator logic changes, regenerate artifacts before app/test validation.
- Keep code comments short and only where needed for non-obvious logic.

## Verification Minimum

Use at least one compile or test task relevant to changed modules. Examples:

- `gradlew.bat :jParser:jParser-core:compileJava`
- `gradlew.bat :examples:TestLib:app:desktop-jni:test`
- `gradlew.bat :examples:TestLib:app:desktop-ffm:test`

## Handoff Checklist

- `LOCAL_AGENT.md` includes:
  - what changed
  - why it changed
  - validation result
  - next recommended task
- Mention any environment blockers explicitly (toolchain, missing native artifacts, classifier jars).

