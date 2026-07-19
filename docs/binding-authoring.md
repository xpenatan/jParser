# Binding Authoring

jParser can generate bindings from native directive blocks embedded in Java and from WebIDL interfaces. This guide covers the author-facing syntax and ownership rules; generator internals are documented in [Architecture](architecture.md).

## Native Directive Blocks

In a `base` Java source file, annotate a native code comment with one or more targets and a command. jParser applies the block while generating the selected target.

```java
public class MyLib extends IDLBase {

    // Replaces this method for TeaVM web.
    /*[-TEAVM;-REPLACE]
        @org.teavm.jso.JSBody(params = {"this_addr"},
            script = "var jsObj = [MODULE].wrapPointer(this_addr, [MODULE].MyType);"
                   + "return jsObj.getValue();")
        private static native int internal_native_getValue(int this_addr);
    */

    // Compiles this implementation into the JNI native library.
    /*[-JNI;-NATIVE]
        MyType* obj = (MyType*)this_addr;
        return obj->getValue();
    */
    private static native int internal_native_getValue(long this_addr);
}
```

Supported target headers are `JNI`, `FFM`, `TEAVM`, and `TEAVM_C`.

| Command | Effect |
|---|---|
| `-NATIVE` | Attaches inline C/C++ code to the following Java `native` method |
| `-ADD` | Adds processed code to the generated output |
| `-ADD_RAW` | Adds code without processing |
| `-REMOVE` | Removes code from the generated output |
| `-REPLACE` | Replaces the following method |
| `-REPLACE_BLOCK` | Replaces the following code block |
| `-IDL_SKIP` | Skips IDL generation for the annotated class |

## WebIDL Bindings

WebIDL is useful when a native API contains many classes and methods. Define an `.idl` interface and jParser generates the corresponding Java and native binding layers:

```idl
interface NormalClass {
    void NormalClass();
    long addIntValue(long value1, long value2);
    static long subIntValue(long value1, long value2);
    attribute long intValue;
    attribute float floatValue;
};
```

The same definition can generate JNI, FFM, TeaVM web, and TeaVM C bindings.

Important WebIDL behaviors:

- Helper classes such as `IDLInt` and `IDLIntArray` pass primitive pointers across Emscripten, desktop, and mobile bindings.
- C++ enums become Java enums containing the corresponding integer value.
- Methods marked `[Value]` return a cached wrapper. The cache is overwritten by the next value call, so callers must not retain it.
- Classes marked `[NoDelete]` do not own their native object and must not call `dispose()` for it.

## `IDLBase` Ownership

Generated native wrappers extend `IDLBase`. jParser does not automatically dispose owned C++ objects: call `dispose()` when the object is no longer needed. Only objects you create or explicitly take ownership of should be disposed.

| API | Purpose |
|---|---|
| `ClassName.native_new()` | Creates an empty wrapper without native data |
| `ClassName.NULL` | Provides a null native wrapper for APIs that cannot use Java `null` |
| `dispose()` | Deletes the owned native object |
| `isDisposed()` | Reports whether the wrapper has been disposed |
| `native_setVoid(...)` | Assigns an integer or long native address |
| `native_reset()` | Resets the wrapper to its default state |
| `native_takeOwnership()` | Makes the wrapper responsible for native deletion |
| `native_releaseOwnership()` | Releases deletion responsibility |
| `native_hasOwnership()` | Reports whether the wrapper owns the native object |
| `native_copy(...)` | Copies the address and native metadata from another wrapper |

The `native_` prefix prevents collisions with methods exposed by the bound C/C++ API. Creating and deleting native objects can be expensive, so avoid doing it every frame.
