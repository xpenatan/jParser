# jParser

![Build](https://github.com/xpenatan/jParser/actions/workflows/snapshot.yml/badge.svg)
[![Maven Central Version](https://img.shields.io/maven-central/v/com.github.xpenatan.jParser/jParser-core)](https://central.sonatype.com/artifact/com.github.xpenatan.jParser/jParser-core)
[![Static Badge](https://img.shields.io/badge/snapshot---SNAPSHOT-red)](https://central.sonatype.com/service/rest/repository/browse/maven-snapshots/com/github/xpenatan/jParser/)


jParser is a compact Java library designed to facilitate the integration of C/C++ code with desktop, mobile, and web platforms, enabling inline writing within Java source code.

Inspired by [gdx-jnigen](https://github.com/libgdx/gdx-jnigen), jParser allows you to embed native code within a code block. This block is then translated into the appropriate target-specific code. You can define multiple code block targets within the same Java source file, and for each target, jParser generates a corresponding Java source file.

For web applications, jParser requires Emscripten to produce JS/WASM files and utilizes [TeaVM](https://github.com/konsoletyper/teavm). The classes generated in the TeaVM module use `JSBody` annotation solution to interact with JavaScript.

Currently, jParser supports only `JNI` and `TEAVM` code targets. There are plans to support the Java Foreign Function and Memory API (FFM).

## How it Works
jParser consists of two main components:

1. **Code Generation**: It reads the Java source code containing the jParser solution and generates new Java source code for each target platform. The `base` module is used for this purpose. For desktop and mobile platforms, the generated JNI code is located in the `core` module, while the web-specific code is placed in the `teavm` module.

2. **C/C++ Compilation**: It compiles the C/C++ code for various platforms, including Windows, Linux, macOS, Android, iOS, and the Web.

## WebIDL
To further streamline the lengthy process of manually porting each method, jParser includes support for Emscripten WebIDL. By creating a WebIDL file, you can automatically generate binding code for both JNI and TeaVM. While this feature may not cover every scenario, it significantly reduces the effort required to bind large libraries. For a comprehensive example, refer to the `example:lib` module or `gdx-imgui`.

The generated methods will match those defined in the WebIDL file. If the C++ code is case-sensitive, as seen in ImGui, the corresponding Java methods will also maintain case sensitivity. Additionally, C/C++ attributes are converted into methods prefixed with `set_` or `get_`.

## WebIDL Notes
* IDL classes, such as IDLInt or IDLIntArray, provide a way to pass primitive pointers to C++ code. These classes are compatible with Emscripten, desktop, and mobile platforms. Use them when you need to pass a pointer array or a primitive that the C++ code will modify.
* C++ enums are converted into Java Enums, where each enum name contains the integer value from the native code.
* Methods annotated with [Value] return a copy of the object. The object is cached in both C++ and Java. Each time you call the same method, it overwrites the previous data, so avoid retaining references to the returned object.
* JParser does not automatically dispose C++ objects. It's your responsibility to call dispose to free the memory. For classes marked with [NoDelete], there is no need to call dispose.

## IDLBase methods
Every native class extends IDLBase, a parent class that provides common functionality. One commonly used method is dispose, which frees the memory allocated for a native object to prevent memory leaks. 
You must call this method when you're done using it. However, not all classes require calling dispose. Only objects you create manually, or those created by a library and explicitly owned by you, need to have their dispose method called. 
Creating a native class and disposing is expensive, so avoid calling these operations every frame. <br><br>
Here is a list of all IDLBase methods:
* **static [ClassName].native_new()**: Creates a new empty instance without any associated native data.
* **static [ClassName].NULL**: Returns a NULL instance. Every method parameter must not be null. Use this when a native methods needs a null parameter.
* **dispose()**: Deletes the native instance, but only if you own it.
* **isDisposed()**: Checks whether the native instance has been disposed.
* **native_setVoid(...)**: Sets an integer or long memory address. In TeaVM, the long is cast to an integer.
* **native_reset()**: Resets the Java instance to its default state, removing any associated native data.
* **native_takeOwnership()**: Takes ownership of the native data, enabling dispose() to delete the object.
* **native_releaseOwnership()**: Releases ownership of the native data, preventing dispose() from deleting the object.
* **native_hasOwnership()**: Checks whether you own the native instance.
* **native_copy(...)**: Copies the memory address and all other native data from another Java instance to this Java instance.

The **native** method keyword is primarily used to avoid conflicts with C/C++ methods.

## Libraries using jParser: <br>
- [jWebGPU](https://github.com/xpenatan/jWebGPU)¹
- [gdx-imgui](https://github.com/xpenatan/gdx-imgui)¹
- [gdx-jolt](https://github.com/xpenatan/gdx-jolt)¹
- [gdx-lua](https://github.com/xpenatan/gdx-lua)¹
- [gdx-box2d](https://github.com/xpenatan/gdx-box2d)²
- [gdx-bullet](https://github.com/xpenatan/gdx-bullet)²
- [gdx-physx](https://github.com/xpenatan/gdx-physx)²

¹: The focus is on maintaining this project. <br>
²: This project is currently inactive and may only be used to test the generator.

## Requirements:
#### [Mingw64](https://github.com/niXman/mingw-builds-binaries/releases) or [Visual Studio C++](https://visualstudio.microsoft.com/vs/community/)
#### [Emscripten](https://emscripten.org/)

