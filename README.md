# jParser

![Build](https://github.com/xpenatan/jParser/actions/workflows/release.yml/badge.svg)
![Build](https://github.com/xpenatan/jParser/actions/workflows/snapshot.yml/badge.svg)
[![Sonatype Nexus (Snapshots)](https://img.shields.io/nexus/releases/com.github.xpenatan.jParser/jParser-core?nexusVersion=2&server=https%3A%2F%2Foss.sonatype.org&label=release)](https://repo.maven.apache.org/maven2/com/github/xpenatan/jParser/)
[![Static Badge](https://img.shields.io/badge/snapshot---SNAPSHOT-red)](https://oss.sonatype.org/content/repositories/snapshots/com/github/xpenatan/jParser/)

jParser is a compact Java library designed to facilitate the integration of C/C++ code with desktop, mobile, and web platforms, enabling inline writing within Java source code.

Inspired by [gdx-jnigen](https://github.com/libgdx/gdx-jnigen), jParser allows you to embed native code within a code block. This block is then translated into the appropriate target-specific code. You can define multiple code block targets within the same Java source file, and for each target, jParser generates a corresponding Java source file.

For web applications, jParser requires Emscripten to produce JS/WASM files and utilizes [TeaVM](https://github.com/konsoletyper/teavm). The classes generated in the TeaVM module use `JSBody` annotation solution to interact with JavaScript.

Currently, jParser supports only `JNI` and `TEAVM` code targets.

### How it Works
jParser consists of two main components:

1. **Code Generation**: It reads the Java source code containing the jParser solution and generates new Java source code for each target platform. The `base` module is used for this purpose. For desktop and mobile platforms, the generated JNI code is located in the `core` module, while the web-specific code is placed in the `teavm` module.

2. **C/C++ Compilation**: It compiles the C/C++ code for various platforms, including Windows, Linux, macOS, Android, iOS, and the Web.

## WebIDL
To further streamline the lengthy process of manually porting each method, jParser includes support for Emscripten WebIDL. By creating a WebIDL file, you can automatically generate binding code for both JNI and TeaVM. While this feature may not cover every scenario, it significantly reduces the effort required to bind large libraries. For a comprehensive example, refer to the `example:lib` module or `gdx-imgui`.

The generated methods will match those defined in the WebIDL file. If the C++ code is case-sensitive, as seen in ImGui, the corresponding Java methods will also maintain case sensitivity. Additionally, C/C++ attributes are converted into methods prefixed with `set_` or `get_`.

## WebIDL Classes
IDL classes, such as `IDLInt` or `IDLIntArray`, provide a method for passing primitive pointers to C++ code, compatible with Emscripten, desktop, and mobile platforms. Use these classes when you need to pass a pointer array or a primitive that the C++ code will modify.

Libraries usisng jParser: <br>
- [gdx-imgui](https://github.com/xpenatan/gdx-imgui)
- [gdx-lua](https://github.com/xpenatan/gdx-lua)
- [gdx-jolt](https://github.com/xpenatan/gdx-jolt)
- [gdx-bullet](https://github.com/xpenatan/gdx-bullet)
- [gdx-physx](https://github.com/xpenatan/gdx-physx)
- [gdx-box2d](https://github.com/xpenatan/gdx-box2d)

## Requirements:
#### [Mingw64](https://github.com/niXman/mingw-builds-binaries/releases) or [Visual Studio C++](https://visualstudio.microsoft.com/vs/community/)
#### [Emscripten](https://emscripten.org/)

