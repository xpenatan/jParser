cmake_minimum_required(VERSION 3.13 FATAL_ERROR)
set(CMAKE_POLICY_DEFAULT_CMP0077 NEW)

FIND_PACKAGE(Python3)
set(PYTHON ${Python3_EXECUTABLE} CACHE STRING "Python path")
set(EMSCRIPTEN_ROOT $ENV{EMSDK}/upstream/emscripten CACHE STRING "Emscripten path")
set(CMAKE_TOOLCHAIN_FILE ${EMSCRIPTEN_ROOT}/cmake/Modules/Platform/Emscripten.cmake)
set(WEBIDL_BINDER_SCRIPT ${EMSCRIPTEN_ROOT}/tools/webidl_binder.py)
set(LIB_HEADER_FILE ${CMAKE_CURRENT_SOURCE_DIR}/exampleLib.h)
set(LIB_IDL_FILE ${CMAKE_CURRENT_SOURCE_DIR}/exampleLib.idl)
set(LIB_SRC_DIR ${CMAKE_CURRENT_SOURCE_DIR}/exampleLib/src)

set(LIB_TARGETS ExampleLib)
foreach(_TARGET ${LIB_TARGETS})
  list(APPEND LIB_LIBS $<TARGET_FILE:${_TARGET}>)
endforeach()

# Build Release by default
set(CMAKE_BUILD_TYPE Release CACHE STRING "Build Type")

# Default is 64*1024*1024 = 64MB
set(INITIAL_MEMORY 67108864 CACHE STRING "Total Memory")

# Enable for resizable heap, with some amount of slowness
set(ALLOW_MEMORY_GROWTH 0 CACHE STRING "Allow Memory Growth")

set(EMCC_ARGS
  -O3
  -std=c++14
  --llvm-lto 1
  --post-js glue.js
  --extern-post-js ../../src/main/cpp/post.js
  --closure 1
  -s ALLOW_MEMORY_GROWTH=1
  -s ALLOW_TABLE_GROWTH=1
  -s EXPORT_NAME="exampleLib"
  -s MODULARIZE=1
  -s NO_FILESYSTEM=1
  -s INITIAL_MEMORY=${INITIAL_MEMORY}
  -s EXPORTED_FUNCTIONS=["_free","_malloc"]
  -s EXPORTED_RUNTIME_METHODS=["UTF8ToString"]
)

set(EMCC_JS_ARGS ${EMCC_ARGS}
  -s SINGLE_FILE=1
  -s WASM=0
)

set(EMCC_WASM_ARGS ${EMCC_ARGS}
  -s WASM=1
  -s SINGLE_FILE=1
)

set(EMCC_GLUE_ARGS
  -c
  -I${LIB_SRC_DIR}
  -include ../../custom_glue.cpp
  -include${LIB_HEADER_FILE}
)

#######################################
project("exampleLib")
add_subdirectory(exampleLib EXCLUDE_FROM_ALL)

#######################################
add_custom_command(
  OUTPUT glue.cpp glue.js
  BYPRODUCTS parser.out WebIDLGrammar.pkl
  COMMAND ${PYTHON} ${WEBIDL_BINDER_SCRIPT} ${LIB_IDL_FILE} glue
  DEPENDS ${LIB_IDL_FILE}
  COMMENT "Generating lib bindings"
  VERBATIM
)
add_custom_command(
  OUTPUT glue.o
  COMMAND em++ glue.cpp ${EMCC_GLUE_ARGS} -o glue.o
  DEPENDS glue.cpp ${LIB_HEADER_FILE}
  COMMENT "Building lib bindings"
  VERBATIM
)
add_custom_target(lib-bindings ALL DEPENDS glue.js glue.o)

#######################################
add_custom_command(
  OUTPUT exampleLib.js
  COMMAND em++ glue.o ${LIB_LIBS} ${EMCC_JS_ARGS} -o exampleLib.js
  DEPENDS lib-bindings ${LIB_TARGETS}
  COMMENT "Building lib javascript"
  VERBATIM)
add_custom_target(exampleLib-javascript ALL DEPENDS exampleLib.js)

#######################################
add_custom_command(
  OUTPUT exampleLib.wasm.js exampleLib.wasm.wasm
  COMMAND emcc glue.o ${LIB_LIBS} ${EMCC_WASM_ARGS} -o exampleLib.wasm.js
  DEPENDS lib-bindings ${LIB_TARGETS}
  COMMENT "Building lib webassembly"
  VERBATIM
)
add_custom_target(exampleLib-wasm ALL DEPENDS exampleLib.wasm.js exampleLib.wasm.wasm)
