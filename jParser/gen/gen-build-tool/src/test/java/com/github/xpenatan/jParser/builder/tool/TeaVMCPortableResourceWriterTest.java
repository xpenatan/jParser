package com.github.xpenatan.jParser.builder.tool;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Pattern;
import org.junit.Test;

public class TeaVMCPortableResourceWriterTest {
    private static final Pattern TEMPLATE_PLACEHOLDER_PATTERN = Pattern.compile("@[A-Z][A-Z0-9_]*@");

    @Test
    public void generatedMarkerSelectsLoaderWithoutAddingADuplicateLoaderMarker() {
        String properties = TeaVMCPortableResourceWriter.generateProperties();

        assertTrue(properties.contains("ignore-resources=META-INF\n"));
        assertTrue(properties.contains("resources=loader-c-\n"));
    }

    @Test
    public void generatedCMakeRendersTheClasspathTemplateWithoutPlaceholders() throws IOException {
        String cmake = TeaVMCPortableResourceWriter.generateCMake("TestLib");

        assertTrue(cmake.contains(
                "if(NOT DEFINED JPARSER_TESTLIB_TEAVMC_ROOT"));
        assertTrue(cmake.contains(
                "get_filename_component(JPARSER_TESTLIB_TEAVMC_EXTERNAL_CPP_ROOT \"${CMAKE_CURRENT_LIST_DIR}/../..\" ABSOLUTE)"));
        assertTrue(cmake.contains(
                "set(JPARSER_TESTLIB_TEAVMC_ROOT \"${JPARSER_TESTLIB_TEAVMC_EXTERNAL_CPP_ROOT}/jparser/testlib\")"));
        assertTrue(cmake.contains("set(JPARSER_TESTLIB_TEAVMC_PLATFORM \"windows_x64\")"));
        assertTrue(cmake.contains("set(JPARSER_TESTLIB_TEAVMC_STATIC_FILE \"TestLib64_.lib\")"));
        assertTrue(cmake.contains(
                "${JPARSER_TESTLIB_TEAVMC_NATIVE_ROOT}/${JPARSER_TESTLIB_TEAVMC_PLATFORM}/${JPARSER_TESTLIB_TEAVMC_STATIC_FILE}"));
        assertFalse(TEMPLATE_PLACEHOLDER_PATTERN.matcher(cmake).find());
    }

    @Test
    public void generatedCMakeSupportsAPortableTargetWithTheGdxTeaVMFallback() throws IOException {
        String cmake = TeaVMCPortableResourceWriter.generateCMake("TestLib");

        assertTrue(cmake.contains("if(NOT DEFINED JPARSER_TEAVMC_APP_TARGET"));
        assertTrue(cmake.contains("set(JPARSER_TEAVMC_APP_TARGET \"${TEAVM_APP_TARGET}\")"));
        assertTrue(cmake.contains(
                "target_sources(${JPARSER_TEAVMC_APP_TARGET} PRIVATE \"${JPARSER_TESTLIB_TEAVMC_GLUE_SOURCE}\")"));
        assertFalse(cmake.contains("target_sources(${TEAVM_APP_TARGET}"));
    }

    @Test
    public void generatedCMakeSupportsAPortableGeneratedSourceRoot() throws IOException {
        String cmake = TeaVMCPortableResourceWriter.generateCMake("TestLib");

        assertTrue(cmake.contains("if(NOT DEFINED JPARSER_TEAVMC_GENERATED_SOURCE_ROOT"));
        assertTrue(cmake.contains(
                "\"${JPARSER_TEAVMC_GENERATED_SOURCE_ROOT}/*.c\""));
        assertFalse(cmake.contains("JPARSER_TESTLIB_TEAVMC_PROJECT_ROOT"));
    }

    @Test
    public void generatedCMakeAllowsAnExplicitPortableStaticLibrary() throws IOException {
        String cmake = TeaVMCPortableResourceWriter.generateCMake("TestLib");

        assertTrue(cmake.contains("if(NOT DEFINED JPARSER_TESTLIB_TEAVMC_LIBRARY"));
        assertTrue(cmake.contains(
                "set JPARSER_TESTLIB_TEAVMC_LIBRARY explicitly"));
        assertTrue(cmake.contains(
                "target_link_libraries(${JPARSER_TEAVMC_APP_TARGET} PRIVATE \"${JPARSER_TESTLIB_TEAVMC_LIBRARY}\")"));
    }

    @Test
    public void generatedCMakeRequiresCxx17WithoutDowngradingTheConsumerTarget() throws IOException {
        String cmake = TeaVMCPortableResourceWriter.generateCMake("TestLib");

        assertTrue(cmake.contains(
                "target_compile_features(${JPARSER_TEAVMC_APP_TARGET} PRIVATE cxx_std_17)"));
        assertFalse(cmake.contains("PROPERTY CXX_STANDARD"));
        assertFalse(cmake.contains("CXX_STANDARD_REQUIRED"));
    }

    @Test
    public void generatedCMakeNeverChangesTheConsumerRuntimeSelection() throws IOException {
        String cmake = TeaVMCPortableResourceWriter.generateCMake("TestLib");

        assertFalse(cmake.contains(
                "target_compile_options(${JPARSER_TEAVMC_APP_TARGET} PRIVATE \"/MT\")"));
        assertFalse(cmake.contains(
                "target_compile_options(${JPARSER_TEAVMC_APP_TARGET} PRIVATE \"/MD\")"));
        assertFalse(cmake.contains(
                "target_compile_definitions(${JPARSER_TEAVMC_APP_TARGET} PRIVATE \"_ITERATOR_DEBUG_LEVEL=0\")"));
        assertFalse(cmake.contains("JPARSER_TEAVMC_MSVC_RUNTIME"));
        assertFalse(cmake.contains(
                "set_property(TARGET ${JPARSER_TEAVMC_APP_TARGET} PROPERTY MSVC_RUNTIME_LIBRARY"));
        assertTrue(cmake.contains(
                "get_target_property(JPARSER_TESTLIB_TEAVMC_MSVC_RUNTIME_VALUE"));
        assertTrue(cmake.contains("CMAKE_MSVC_RUNTIME_LIBRARY"));
        assertTrue(cmake.contains(
                "\"MultiThreaded$<$<CONFIG:Debug>:Debug>DLL\""));
    }

    @Test
    public void generatedCMakeSearchesRuntimeSpecificWindowsPayloadsBeforeLegacyPaths() throws IOException {
        String cmake = TeaVMCPortableResourceWriter.generateCMake("TestLib");

        String runtimeSpecific = "${JPARSER_TESTLIB_TEAVMC_PLATFORM}/${JPARSER_TESTLIB_TEAVMC_MSVC_RUNTIME_SUBDIR}/static/${JPARSER_TESTLIB_TEAVMC_STATIC_FILE}";
        String legacy = "${JPARSER_TESTLIB_TEAVMC_PLATFORM}/${JPARSER_TESTLIB_TEAVMC_STATIC_FILE}";
        assertTrue(cmake.contains(runtimeSpecific));
        assertTrue(cmake.indexOf(runtimeSpecific) < cmake.indexOf(legacy));
        assertTrue(cmake.contains(
                "${JPARSER_TESTLIB_TEAVMC_PLATFORM}/${JPARSER_TESTLIB_TEAVMC_MSVC_RUNTIME_SUBDIR}/shared/${JPARSER_TESTLIB_TEAVMC_SHARED_FILE}"));
    }

    @Test
    public void generatedCMakeAppendsEveryImportHeaderCompileOption() throws IOException {
        assertImportHeaderOptionsAppend(TeaVMCPortableResourceWriter.generateCMake("runtime"), "JPARSER_RUNTIME_TEAVMC");
        assertImportHeaderOptionsAppend(TeaVMCPortableResourceWriter.generateCMake("TestLib"), "JPARSER_TESTLIB_TEAVMC");
    }

    @Test
    public void generatedCMakePreservesStaticDefaultAndSupportsAllLinkageModes() throws IOException {
        String staticCMake = TeaVMCPortableResourceWriter.generateCMake("TestLib", TeaVMCLinkage.STATIC);
        String sharedCMake = TeaVMCPortableResourceWriter.generateCMake("TestLib", TeaVMCLinkage.SHARED_LINKED);
        String runtimeCMake = TeaVMCPortableResourceWriter.generateCMake("TestLib", TeaVMCLinkage.RUNTIME_LOADED);

        assertTrue(staticCMake.contains("set(JPARSER_TESTLIB_TEAVMC_LINKAGE \"STATIC\")"));
        assertTrue(sharedCMake.contains("set(JPARSER_TESTLIB_TEAVMC_LINKAGE \"SHARED_LINKED\")"));
        assertTrue(runtimeCMake.contains("set(JPARSER_TESTLIB_TEAVMC_LINKAGE \"RUNTIME_LOADED\")"));
        assertTrue(runtimeCMake.contains("JPARSER_TEAVMC_LINKAGE_MODE=${JPARSER_TESTLIB_TEAVMC_LINKAGE_VALUE}"));
        assertTrue(runtimeCMake.contains("${JPARSER_TESTLIB_TEAVMC_ABI_ROOT}/TeaVMCDispatch.cpp"));
        assertTrue(runtimeCMake.contains("Runtime-loaded mode deliberately has no link dependency on the plugin"));
        assertTrue(runtimeCMake.contains("APPEND PROPERTY BUILD_RPATH \"@loader_path\""));
        assertTrue(runtimeCMake.contains("APPEND PROPERTY BUILD_RPATH \"$ORIGIN\""));
    }

    @Test
    public void generatedCMakeSupportsSharedPayloadOverridesAndPortableStaging() throws IOException {
        String cmake = TeaVMCPortableResourceWriter.generateCMake("TestLib");

        assertTrue(cmake.contains("JPARSER_TESTLIB_TEAVMC_SHARED_LIBRARY"));
        assertTrue(cmake.contains("JPARSER_TESTLIB_TEAVMC_IMPORT_LIBRARY"));
        assertTrue(cmake.contains("JPARSER_TEAVMC_RUNTIME_OUTPUT_DIRECTORY"));
        assertTrue(cmake.contains("copy_if_different"));
        assertTrue(cmake.contains("${JPARSER_TESTLIB_TEAVMC_NATIVE_ROOT}/${JPARSER_TESTLIB_TEAVMC_PLATFORM}/shared/"));
        assertTrue(cmake.contains("android/${ANDROID_ABI}"));
        assertTrue(cmake.contains("required for future iOS-C layouts"));
    }

    @Test
    public void generatedCMakeUsesTheProducedMacArmStaticArchiveName() throws IOException {
        String cmake = TeaVMCPortableResourceWriter.generateCMake("TestLib");

        assertTrue(cmake.contains("set(JPARSER_TESTLIB_TEAVMC_PLATFORM \"mac_arm64\")"));
        assertTrue(cmake.contains("set(JPARSER_TESTLIB_TEAVMC_STATIC_FILE \"libTestLib64_.a\")"));
        assertFalse(cmake.contains("libTestLibarm64_.a"));
    }

    @Test
    public void generatedCMakeAppliesResourceSelectedConsumerRequirements() throws IOException {
        ArrayList<TeaVMCConsumerConfig> consumers = new ArrayList<>();
        TeaVMCConsumerConfig wgpu = consumer("windows64_teavm_c", "wgpu", "include/webgpu/wgpu.h");
        wgpu.headerDirs.add("include");
        wgpu.staticLibraries.add(new TeaVMCConsumerConfig.StaticLibrary(
                "deps/wgpu_native.lib",
                "TESTLIB_WGPU_LIBRARY"));
        wgpu.staticLinkLibraries.add("user32.lib");
        consumers.add(wgpu);

        TeaVMCConsumerConfig dawn = consumer("windows64_teavm_c", "dawn", "include/dawn/webgpu.h");
        dawn.headerDirs.add("include");
        dawn.compileDefinitions.add("TESTLIB_DAWN=1");
        dawn.compileOptions.add("/Zc:preprocessor");
        dawn.staticLibraries.add(new TeaVMCConsumerConfig.StaticLibrary(
                "deps/webgpu_dawn.lib",
                "TESTLIB_DAWN_LIBRARY"));
        consumers.add(dawn);

        String cmake = TeaVMCPortableResourceWriter.generateCMake(
                "TestLib",
                TeaVMCLinkage.STATIC,
                consumers);

        assertTrue(cmake.contains("JPARSER_TESTLIB_TEAVMC_CONSUMER_MATCH_COUNT"));
        assertTrue(cmake.contains("include/webgpu/wgpu.h"));
        assertTrue(cmake.contains("include/dawn/webgpu.h"));
        assertTrue(cmake.contains("Multiple packaged TestLib TeaVM C consumer variants"));
        assertTrue(cmake.contains("target_compile_definitions(${JPARSER_TEAVMC_APP_TARGET} PRIVATE \"TESTLIB_DAWN=1\")"));
        assertTrue(cmake.contains("target_compile_options(${JPARSER_TEAVMC_APP_TARGET} PRIVATE \"/Zc:preprocessor\")"));
        assertTrue(cmake.contains(
                "${JPARSER_TESTLIB_TEAVMC_PLATFORM}/${JPARSER_TESTLIB_TEAVMC_MSVC_RUNTIME_SUBDIR}/deps/wgpu_native.lib"));
        assertTrue(cmake.contains("if(DEFINED TESTLIB_WGPU_LIBRARY"));
        assertTrue(cmake.contains("target_link_libraries(${JPARSER_TEAVMC_APP_TARGET} PRIVATE \"user32.lib\")"));
        assertFalse(TEMPLATE_PLACEHOLDER_PATTERN.matcher(cmake).find());
    }

    @Test
    public void generatedCMakeKeepsStaticConsumerLibrariesOutOfSharedLinkage() throws IOException {
        ArrayList<TeaVMCConsumerConfig> consumers = new ArrayList<>();
        TeaVMCConsumerConfig consumer = consumer("linux64_teavm_c", "native", "include/native/api.h");
        consumer.staticLibraries.add(new TeaVMCConsumerConfig.StaticLibrary("deps/libnative.a", ""));
        consumer.staticLinkOptions.add("SHELL:-pthread");
        consumers.add(consumer);

        String cmake = TeaVMCPortableResourceWriter.generateCMake(
                "TestLib",
                TeaVMCLinkage.SHARED_LINKED,
                consumers);

        int staticGuard = cmake.indexOf("if(JPARSER_TESTLIB_TEAVMC_LINKAGE STREQUAL \"STATIC\")", cmake.indexOf("CONSUMER_SELECTED"));
        int staticLibrary = cmake.indexOf("deps/libnative.a");
        int linkOption = cmake.indexOf("target_link_options(${JPARSER_TEAVMC_APP_TARGET} PRIVATE \"SHELL:-pthread\")");
        assertTrue(staticGuard >= 0);
        assertTrue(staticLibrary > staticGuard);
        assertTrue(linkOption > staticGuard);
    }

    private static TeaVMCConsumerConfig consumer(String targetName, String variantName, String selector) {
        TeaVMCConsumerConfig consumer = new TeaVMCConsumerConfig();
        consumer.targetName = targetName;
        consumer.variantName = variantName;
        consumer.selectorResources.add(selector);
        return consumer;
    }

    private static void assertImportHeaderOptionsAppend(String cmake, String variablePrefix) {
        String source = "\"${" + variablePrefix + "_SOURCE}\"";
        String importHeader = "${" + variablePrefix + "_ACTIVE_IMPORT_HEADER}";

        assertTrue(cmake.contains("set_property(SOURCE " + source + " APPEND PROPERTY"));
        assertTrue(cmake.contains("COMPILE_OPTIONS \"/FI" + importHeader + "\""));
        assertTrue(cmake.contains("COMPILE_OPTIONS \"-include\" \"" + importHeader + "\""));
        assertFalse(cmake.contains(
                "set_source_files_properties(" + source + " PROPERTIES COMPILE_OPTIONS"));
    }
}
