cmake -G "MinGW Makefiles" -B ./build/emscripten/
cmake --build ./build/emscripten/ -- VERBOSE=1
XCOPY build\emscripten\exampleLib.js ..\..\teavm\src\main\resources\ /y
XCOPY build\emscripten\exampleLib.wasm.js ..\..\teavm\src\main\resources\ /y
XCOPY build\emscripten\exampleLib.wasm.wasm ..\..\teavm\src\main\resources\ /y