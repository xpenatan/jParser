# Agent Architecture Reference

## Core Build Pipeline

Primary orchestration is in `jParser/jParser-build-tool` via `BuilderTool.build()`:

1. IDL parsing (`IDLReader`) from `lib-build/src/main/cpp/*.idl`
2. Core API generation (`IDLDefaultCodeParser`) into `lib-core`
3. JNI generation (`CppCodeParser`) into `lib-jni` and `lib-android`
4. TeaVM web generation (`TeaVMCodeParser` from `gen-web`) into `lib-web`
5. TeaVM C generation (`TeaVMCCodeParser` from `gen-c`) into `*-c` TeaVM C Java bindings plus `TeaVMCGlue.cpp`
6. FFM generation (`FFMCodeParser`) into `lib-ffm`
7. Native compile (`JBuilder.build()`) via platform targets

## Module Conventions

- `*-base`: handwritten Java with target-specific comment blocks
- `*-build`: Gradle entry for generation + native build
- `*-core`: generated bridge-agnostic API
- `*-jni`: generated JNI Java + desktop JNI natives
- `*-ffm`: generated FFM Java + desktop FFM natives
- `*-android`: generated Android JNI output
- `*-web`: generated TeaVM web output
- `*-c/core`: generated TeaVM C Java output
- `*-c/desktop`: TeaVM C desktop native payloads
- `*-c/android`: TeaVM C Android native payloads

Runtime modules keep shared API/build modules at the top level and group implementation modules by runtime family:

- `runtime-core`: public/shared runtime API.
- `runtime-build`: generator and native build driver.
- `runtime-jvm/jni`, `runtime-jvm/ffm`, `runtime-jvm/web`, `runtime-jvm/android`: JVM/Java-side implementation modules published as `runtime-jni`, `runtime-ffm`, `runtime-web`, and `runtime-android`.
- `runtime-c/core`: TeaVM C generated Java classes published as `runtime-c`.
- `runtime-c/desktop`: desktop native-only split artifacts such as `runtime-c_windows_x64`.
- `runtime-c/android`: Android TeaVM C native packaging published as `runtime-c_android`.

Example app modules in examples use:

- `app:core` for shared app code.
- `app:assets` where an example has shared assets.
- `app:platforms:desktop-jni`
- `app:platforms:desktop-ffm`
- `app:platforms:desktop-c`
- `app:platforms:web`
- `app:platforms:android`
- `app:platforms:android-c`

Pattern repeats across `examples/`, `idl/`, `loader/`, and `jParser/` modules (see `settings.gradle.kts`).

## JNI vs FFM

### JNI

- Java side: generated native method stubs.
- Native side: JNI ABI (`JNIEnv*`, `jlong`, `jint`, etc.).
- Parser/generator: `jParser/jParser-jni`.

### FFM

- Java side: `java.lang.foreign` downcalls via MethodHandles.
- Native side: C ABI (`extern "C"`, `int64_t`, `int32_t`, no `JNIEnv*`).
- Parser/generator: `jParser/jParser-ffm`.
- Build option is off by default (`BuildToolOptions.generateFFM=false`) unless enabled by task/config.

### TeaVM C

- Java side: static native methods annotated with `org.teavm.interop.Import`.
- Native side: C ABI (`extern "C"`, `int64_t`, `int32_t`, no `JNIEnv*`).
- Parser/generator: `TeaVMCCodeParser` with `TeaVMCGenerator` in `jParser:gen:gen-c`.
- Build option is off by default (`BuildToolOptions.generateTeaVMC=false`) unless enabled with `gen_teavm_c`.
- Generated Java is written to the C output path (`BuildToolOptions.getCJavaOutputPath()`), not the TeaVM web output path.
- Native libraries are selected by platform target args such as `windows64_teavm_c`, `android_teavm_c`, or `ios_teavm_c`.
- IDL callback implementation glue is generated with TeaVM C imports/exports and C function pointers when callbacks are present.

## Native Comment Block Contract (`lib-base`)

Supported headers: `JNI`, `FFM`, `TEAVM`, `TEAVM_C`.

Supported commands: `-ADD`, `-ADD_RAW`, `-REMOVE`, `-REPLACE`, `-REPLACE_BLOCK`, `-NATIVE`.

Example:

```java
/*[-JNI;-NATIVE]
    MyType* obj = (MyType*)this_addr;
    obj->doSomething();
*/
/*[-FFM;-NATIVE]
    MyType* obj = (MyType*)this_addr;
    obj->doSomething();
*/
private static native void internal_native_doSomething(long this_addr);
```

`-NATIVE` attaches to the next Java `native` method declaration.

## Toolchain Notes

- JDK 11+ required for core build tooling.
- TeaVM 0.15+ web modules and compiler/tooling require JDK 17+.
- FFM tasks require newer JDK (22+; 25 preferred by project docs).
- Desktop FFM launcher tasks in examples use `LibExt.javaFFMTarget` from Gradle configuration.
