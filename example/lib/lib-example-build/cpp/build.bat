cmake -G "MinGW Makefiles" -B ./build/emscripten/
cmake --build ./build/emscripten/ -- VERBOSE=1
XCOPY build\emscripten\exampleLib.js ..\..\lib-example-teavm\src\main\resources\ /y
XCOPY build\emscripten\exampleLib.wasm.js ..\..\lib-example-teavm\src\main\resources\ /y
XCOPY build\emscripten\exampleLib.wasm.wasm ..\..\lib-example-teavm\src\main\resources\ /y