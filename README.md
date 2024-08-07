# jParser

![Build](https://github.com/xpenatan/jParser/actions/workflows/release.yml/badge.svg)
![Build](https://github.com/xpenatan/jParser/actions/workflows/snapshot.yml/badge.svg)
[![Sonatype Nexus (Snapshots)](https://img.shields.io/nexus/releases/com.github.xpenatan.jParser/jParser-core?nexusVersion=2&server=https%3A%2F%2Foss.sonatype.org&label=release)](https://repo.maven.apache.org/maven2/com/github/xpenatan/jParser/)
[![Static Badge](https://img.shields.io/badge/snapshot---SNAPSHOT-red)](https://oss.sonatype.org/content/repositories/snapshots/com/github/xpenatan/jParser/)

jParser is a small Java library that helps bind C/C++ code to desktop, mobile, and the web, allowing it to be written inline with Java source code.

It was inspired by [gdx-jnigen](https://github.com/libgdx/gdx-jnigen) that you add a native code into a code block. This code block will be translated to the specific code target. You can add multiple code block targets in the same java source. For each code target, it will generate a new java source code.

For the web, it needs emscripten to generate a js/wasm file and [teavm](https://github.com/konsoletyper/teavm). The generated classes in teavm module use JSBody to communicate with javascript.

jParser only supports ```JNI``` and ```TEAVM``` code targets.

### How it works
jParser has two part.
* Read the java source code containing the jParser solution and generate a new java source code for each target. We use base module for this. For desktop/mobile the generated JNI code will be at ```core module``` and the web code will be inside ```teavm module```.
* Compile C/C++ for each platform (Windows/Linux/MacOS/Android/iOS/Web). 

## WebIDL
To improve even more the long hours of porting each method manually, jParser also has Emscripten WebIDL support. You create a webidl file, and it will generate binding code for JNI and teaVM. 
It's not 100%, but it will reduce the amount of work binding big libraries. You can check the example:lib module or gdx-imgui for a complete example.

It will create the exact same method as the webidl file. If C++ is case-sensitive as in ImGui, the Java methods will be also case-sensitive. <br>
C/C++ attributes are converted to methods, it starts with 'set_' or 'get_'.

Libraries usisng jParser: <br>
- [gdx-imgui](https://github.com/xpenatan/gdx-imgui)
- [gdx-lua](https://github.com/xpenatan/gdx-lua)
- [gdx-jolt](https://github.com/xpenatan/gdx-jolt)
- [gdx-bullet](https://github.com/xpenatan/gdx-bullet)
- [gdx-physx](https://github.com/xpenatan/gdx-physx)
- [gdx-box2d](https://github.com/xpenatan/gdx-box2d)

## Requirements:
#### [mingw64](https://github.com/niXman/mingw-builds-binaries/releases)
#### [emscripten](https://emscripten.org/)
