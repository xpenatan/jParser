# Getting Started

The [`examples/TestLib`](../examples/TestLib) project is the smallest complete jParser example. It demonstrates the normal source, generation, native packaging, and application modules.

## Project Layout

jParser projects are centered on a hand-written API, a generator entry point, and generated or platform-specific outputs:

| Module | Purpose |
|---|---|
| `base` | Hand-written Java API with native directive blocks |
| `builder` | IDL, native source, target configuration, generation, and compilation entry point |
| `core` | Generated bridge-agnostic Java API; do not hand-edit |
| `shared/<Lib>-jni` | Generated JNI Java shared by desktop and Android; do not hand-edit |
| `shared/<Lib>-c` | Generated TeaVM C Java and portable native resources; do not hand-edit |
| `desktop/<Lib>-desktop-jni` | Desktop JNI native packaging |
| `desktop/<Lib>-desktop-ffm` | Generated desktop FFM Java and native packaging; do not hand-edit |
| `desktop/<Lib>-desktop-c` | Desktop TeaVM C native packaging |
| `web/<Lib>-web` | Generated TeaVM web output; do not hand-edit |
| `android/<Lib>-android` | Android JNI packaging |
| `android/<Lib>-android-c` | Android TeaVM C packaging |

Edit the `base`, `builder`, and native packaging inputs. Regenerate derived Java instead of changing generated output directly.

## Build TestLib

Run commands from the repository root. On Windows use `gradlew.bat`; on Linux and macOS use `./gradlew`.

### JNI

```text
# Build the shared runtime once for the target platform.
./gradlew :jParser:runtime:builder:runtime_helper_build_project_windows64_jni

# Generate TestLib and compile its JNI native library.
./gradlew :examples:TestLib:lib:builder:TestLib_build_project_windows64_jni

# Run the desktop application.
./gradlew :examples:TestLib:app:platforms:desktop-jni:TestLib_run_app_desktop_jni
```

### FFM

```text
# Build the shared runtime once for the target platform.
./gradlew :jParser:runtime:builder:runtime_helper_build_project_windows64_ffm

# Generate TestLib and compile its FFM native library.
./gradlew :examples:TestLib:lib:builder:TestLib_build_project_windows64_ffm

# Run the desktop application.
./gradlew :examples:TestLib:app:platforms:desktop-ffm:TestLib_run_app_desktop_ffm
```

Replace `windows64` with `linux64`, `mac64`, or `macArm` for another desktop host. FFM tasks require the Java toolchain selected by `LibExt.javaFFMTarget`.

## Next Steps

- Read [Binding Authoring](binding-authoring.md) before adding Java/native methods or WebIDL interfaces.
- Read the [TeaVM C guide](teavm-c.md) when producing native TeaVM applications or packaged native dependencies.
- Use the [command reference](commands.md) for web, Android, iOS, TeaVM C, packaging, and benchmark tasks.
- Use the [architecture reference](architecture.md) when changing the generator or module pipeline.
