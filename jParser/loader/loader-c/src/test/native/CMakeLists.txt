cmake_minimum_required(VERSION 3.15)
project(jparser_teavmc_loader_smoke LANGUAGES C CXX)

enable_testing()

set(LOADER_RESOURCE_ROOT "${CMAKE_CURRENT_LIST_DIR}/../../main/resources/external_cpp/jparser/loader")

add_library(jparser_teavmc_test_plugin SHARED plugin.cpp)
target_include_directories(jparser_teavmc_test_plugin PRIVATE "${LOADER_RESOURCE_ROOT}")
target_compile_features(jparser_teavmc_test_plugin PRIVATE cxx_std_17)

add_executable(jparser_teavmc_loader_smoke
  loader_smoke.cpp
  generated/loader_import_call.c)
set(JPARSER_TEAVMC_APP_TARGET jparser_teavmc_loader_smoke)
set(JPARSER_TEAVMC_GENERATED_SOURCE_ROOT "${CMAKE_CURRENT_LIST_DIR}/generated")
include("${CMAKE_CURRENT_LIST_DIR}/../../main/resources/external_cpp/cmake/post_target/jparser_00_teavmc_loader.cmake")

add_test(NAME jparser_teavmc_loader_smoke
  COMMAND jparser_teavmc_loader_smoke
    "$<TARGET_FILE_DIR:jparser_teavmc_test_plugin>"
    "$<TARGET_FILE_NAME:jparser_teavmc_test_plugin>")
file(MAKE_DIRECTORY "${CMAKE_CURRENT_BINARY_DIR}/loader_smoke_working")
set_tests_properties(jparser_teavmc_loader_smoke PROPERTIES
  WORKING_DIRECTORY "${CMAKE_CURRENT_BINARY_DIR}/loader_smoke_working")
