cmake -G "MinGW Makefiles" -B ./build/emscripten/
cmake --build ./build/emscripten/ -- VERBOSE=1
XCOPY build\emscripten\exampleLib.js ..\..\example-teavm\src\main\resources\ /y
XCOPY build\emscripten\exampleLib.wasm.js ..\..\example-teavm\src\main\resources\ /y
XCOPY build\emscripten\exampleLib.wasm.wasm ..\..\example-teavm\src\main\resources\ /y