# Agent Architecture Reference

## Core Build Pipeline

Primary orchestration is in `jParser/jParser-build-tool` via `BuilderTool.build()`:

1. IDL parsing (`IDLReader`) from `lib-build/src/main/cpp/*.idl`
2. Core API generation (`IDLDefaultCodeParser`) into `lib-core`
3. JNI generation (`CppCodeParser`) into `lib-desktop-jni` and `lib-android`
4. TeaVM generation (`TeaVMCodeParser`) into `lib-teavm`
5. FFM generation (`FFMCodeParser`) into `lib-desktop-ffm`
6. Native compile (`JBuilder.build()`) via platform targets

## Module Conventions

- `*-base`: handwritten Java with target-specific comment blocks
- `*-build`: Gradle entry for generation + native build
- `*-core`: generated bridge-agnostic API
- `*-desktop-jni`: generated JNI Java + desktop JNI natives
- `*-desktop-ffm`: generated FFM Java + desktop FFM natives
- `*-android`: generated Android JNI output
- `*-teavm`: generated TeaVM output

Pattern repeats in `examples/`, `idl/`, `loader/`, and `jParser/` modules.

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

## Native Comment Block Contract (`lib-base`)

Supported headers: `JNI`, `FFM`, `TEAVM`.

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
- FFM tasks require newer JDK (22+; 25 preferred by project docs).
- Desktop FFM launcher tasks in examples currently use Java toolchain 24 in those Gradle scripts.
# Agent Architecture Reference

## Core Build Pipeline

Primary orchestration is in `jParser/jParser-build-tool` via `BuilderTool.build()`:

1. IDL parsing (`IDLReader`) from `lib-build/src/main/cpp/*.idl`
2. Core API generation (`IDLDefaultCodeParser`) into `lib-core`
3. JNI generation (`CppCodeParser`) into `lib-desktop-jni` and `lib-android`
4. TeaVM generation (`TeaVMCodeParser`) into `lib-teavm`
5. FFM generation (`FFMCodeParser`) into `lib-desktop-ffm`
6. Native compile (`JBuilder.build()`) via platform targets

## Module Conventions

- `*-base`: handwritten Java with target-specific comment blocks
- `*-build`: Gradle entry for generation + native build
- `*-core`: generated bridge-agnostic API
- `*-desktop-jni`: generated JNI Java + desktop JNI natives
- `*-desktop-ffm`: generated FFM Java + desktop FFM natives
- `*-android`: generated Android JNI output
- `*-teavm`: generated TeaVM output

Pattern repeats in `examples/`, `idl/`, `loader/`, and `jParser/` modules.

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

## Native Comment Block Contract (`lib-base`)

Supported headers: `JNI`, `FFM`, `TEAVM`.

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
- FFM tasks require newer JDK (22+; 25 preferred by project docs).
- Desktop FFM launcher tasks in examples currently use Java toolchain 24 in those Gradle scripts.

