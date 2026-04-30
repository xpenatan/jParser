# Workflows

Startup/session protocol is defined in `AGENTS.md` (`Start Here (Mandatory)`). This file only covers execution details.

## Editing Rules

- Do not hand-edit generated outputs in `lib-core`, `lib-desktop-jni`, `lib-desktop-ffm`, `lib-android`, `lib-teavm-web`.
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
- Keep `LOCAL_AGENT.md` as rolling current state: replace/update prior entries instead of appending history.
- Mention any environment blockers explicitly (toolchain, missing native artifacts, classifier jars).


