cmake_minimum_required(VERSION 3.18)
project(TestLibIOSC C CXX OBJC)

set(CMAKE_C_STANDARD 11)
set(CMAKE_CXX_STANDARD 17)
set(CMAKE_OSX_DEPLOYMENT_TARGET "14.0" CACHE STRING "Minimum iOS version")

set(JPARSER_TEAVMC_GENERATED_SOURCE_ROOT "${CMAKE_CURRENT_SOURCE_DIR}/build/ios-c/c/src")
set(TEAVM_APP_TARGET testlib_teavm_program)
set(JPARSER_TEAVMC_APP_TARGET ${TEAVM_APP_TARGET})

add_library(${TEAVM_APP_TARGET} STATIC
  "${JPARSER_TEAVMC_GENERATED_SOURCE_ROOT}/app_include.c")

if(CMAKE_C_COMPILER_ID MATCHES "Clang")
  target_compile_options(${TEAVM_APP_TARGET} PRIVATE
    -Wno-incompatible-pointer-types-discards-qualifiers
    -Wno-parentheses-equality
    -Wno-pointer-sign
    -Wno-unused-value)
endif()

# The extracted loader/runtime/TestLib resources provide these hooks. They
# expose packaged iOS C compatibility and generated glue, then select the
# packaged SDK/architecture archive slice.
set(TEAVM_IOS_STATIC_DEPENDENCY_TARGETS "")
set(TEAVM_EXTENSION_POST_TARGET_DIR
  "${CMAKE_CURRENT_SOURCE_DIR}/build/ios-c/c/external_cpp/cmake/post_target")
file(GLOB TEAVM_EXTENSION_POST_TARGET_FILES
  "${TEAVM_EXTENSION_POST_TARGET_DIR}/*.cmake")
list(SORT TEAVM_EXTENSION_POST_TARGET_FILES)
foreach(TEAVM_EXTENSION_POST_TARGET_FILE IN LISTS TEAVM_EXTENSION_POST_TARGET_FILES)
  include("${TEAVM_EXTENSION_POST_TARGET_FILE}")
endforeach()

# Flatten only the packaged dependency archives into one consumer-side archive.
# Link the generated program target normally so CMake owns its Xcode
# configuration-dependent path.
set(TEAVM_IOS_BUNDLE_ARCHIVE "${CMAKE_BINARY_DIR}/libTestLibIOSC_bundle.a")
set(TEAVM_IOS_BUNDLE_INPUTS "")
set(TEAVM_IOS_BUNDLE_DEPENDS "")
foreach(TEAVM_IOS_DEPENDENCY_TARGET IN LISTS TEAVM_IOS_STATIC_DEPENDENCY_TARGETS)
  list(APPEND TEAVM_IOS_BUNDLE_INPUTS "$<TARGET_FILE:${TEAVM_IOS_DEPENDENCY_TARGET}>")
  list(APPEND TEAVM_IOS_BUNDLE_DEPENDS ${TEAVM_IOS_DEPENDENCY_TARGET})
endforeach()

add_custom_command(
  OUTPUT "${TEAVM_IOS_BUNDLE_ARCHIVE}"
  COMMAND "${CMAKE_COMMAND}" -E rm -f "${TEAVM_IOS_BUNDLE_ARCHIVE}"
  COMMAND /usr/bin/libtool -static -o "${TEAVM_IOS_BUNDLE_ARCHIVE}" ${TEAVM_IOS_BUNDLE_INPUTS}
  DEPENDS ${TEAVM_IOS_BUNDLE_DEPENDS}
  VERBATIM)
add_custom_target(testlib_teavm_bundle ALL DEPENDS "${TEAVM_IOS_BUNDLE_ARCHIVE}")

add_executable(TestLibIOSC MACOSX_BUNDLE "src/main/ios/Launcher.m")
add_dependencies(TestLibIOSC testlib_teavm_bundle)
target_link_libraries(TestLibIOSC PRIVATE
  ${TEAVM_APP_TARGET}
  "${TEAVM_IOS_BUNDLE_ARCHIVE}"
  "-framework Foundation"
  "-framework UIKit")
set_target_properties(TestLibIOSC PROPERTIES
  LINKER_LANGUAGE CXX
  MACOSX_BUNDLE_INFO_PLIST "${CMAKE_CURRENT_SOURCE_DIR}/src/main/ios/Info.plist"
  XCODE_ATTRIBUTE_CLANG_ENABLE_OBJC_ARC YES
  XCODE_ATTRIBUTE_PRODUCT_BUNDLE_IDENTIFIER "com.github.xpenatan.jparser.testlib.iosc"
  XCODE_ATTRIBUTE_TARGETED_DEVICE_FAMILY "1,2")
