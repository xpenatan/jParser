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

## JNI Callback Threads and Borrowed Values

Generated JNI callbacks execute on the thread that invokes the native callback.
They keep a `JavaVM` reference and obtain that thread's `JNIEnv` on each call.
A detached native thread is attached for the callback and detached afterward;
a thread already attached by Java or the native caller remains attached. This
follows the [JNI invocation API](https://docs.oracle.com/en/java/javase/25/docs/specs/jni/invocation.html#attaching-to-the-vm).
Temporary Java string references are released after each invocation.

Exceptions remain pending for an existing Java caller or attached native caller
to handle. If the callback attached the thread itself, an exception is reported
through JNI's `ExceptionDescribe`, cleared before detachment, and a value-returning
callback returns its zero/default value.

The generated native callback owns a global reference to its Java target until
native destruction. Stop callback producers and wait for active invocations to
finish before disposing the callback. Disposal must precede JVM shutdown; garbage
collection alone cannot release a target held by that global reference. Handwritten
JNI callback blocks must implement equivalent thread and reference handling themselves.

JNI by-value method and arithmetic-operator returns use one native temporary per
method per thread. Another thread cannot overwrite it, but the next call to the
same method on the same thread can; the temporary also expires when its thread
exits. These remain borrowed values. This does not make shared Java wrapper caches
or underlying native objects thread-safe: use independent wrappers and obey the
native library's synchronization rules.

Regenerate and rebuild JNI bindings to receive these generator changes.

## Native String Input Lifetime

Generated FFM calls encode Java `String` arguments in one confined arena owned
by that call. All arguments remain valid through the native invocation and
any conversion of its return value. The arena closes on return or exception;
null arguments become null native pointers. Calls without String arguments
do not create a string arena.

By default, native functions borrow these pointers only for the duration of
the call, matching the existing JNI input lifetime. A native object that needs
the text afterward must copy it into owned storage, such as `std::string`,
or explicitly accept transferred ownership as described below.

Regenerate FFM bindings when upgrading from the cached conversion generator.
The old `NativeUtils.toCString(String)` helper and `jparser.ffm.stringCacheSize`
setting are removed. Previously generated calls to that helper must be
regenerated together with the runtime upgrade.

### Transferring a String input to C++

Mark a scalar `DOMString` parameter on an IDL method or constructor with
`OWNED_STRING=parameterName`:

```webidl
void retain(DOMString text); //[-OWNED_STRING=text]
void retainPair(DOMString first, DOMString second); //[-OWNED_STRING=first, OWNED_STRING=second]
```

JNI, FFM, TeaVM C and Emscripten native glue copy each marked input into an
independent `new char[]` buffer before calling C++. The original Java-to-native
temporary still follows its normal cleanup. Ownership of the copy passes to
the native callee when it is invoked; the binding does not free that copy.
Unmarked parameters keep the default borrowed lifetime. `null` transfers a
null pointer; an empty String transfers a separately allocated terminator.
This command preserves the backend's existing string encoding.

The C++ callee must eventually use **`delete[]`** in the compatible native
allocation domain, including when replacing or rejecting a value. For example:

```cpp
std::unique_ptr<const char[]> text;
void retain(const char* value) { text.reset(value); }
```

Only use this command for an API that takes ownership with that allocation
contract. It does not make a container of raw pointers free its elements, and
must not be used with an API that expects `free()` or another allocator.
Callback inputs, attributes, arrays and non-string parameters do not support
this command. Optional overloads retain the setting for parameters they expose.
Handwritten native replacement blocks must implement their own ownership
contract; the command applies to generated IDL calls. Regenerate the bindings
and rebuild their native libraries when adding or removing it.

## Binding Class Finality

Generated IDL binding classes are `final` by default. Configure the global
setting in the Gradle plugin DSL:

```kotlin
jParser {
    finalClass.set(false)
}
```

Override one class independently with a Boolean value:

```kotlin
jParser {
    finalClass.set(false)
    finalClass("WGPUBuffer", true)
    finalClass("ExtensibleType", false)
}
```

For manual builders, use the equivalent `BuildToolOptions` API:

```java
buildToolOptions.finalClass = false;
buildToolOptions.setFinalClass("WGPUBuffer", true);
buildToolOptions.setFinalClass("ExtensibleType", false);
```

The class-specific value takes precedence over the global value. Callback
classes and binding classes that are extended by another class remain
non-final.

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
