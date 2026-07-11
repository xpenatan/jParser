package com.github.xpenatan.jParser.c;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class TeaVMCRuntimeArtifactsTest {

    @Rule
    public final TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void generatesDeterministicTypedProviderAndMacroDispatch() throws Exception {
        Path first = generatedRoot("first", glue(false));
        Path second = generatedRoot("second", glue(true));

        TeaVMCRuntimeArtifacts.generate(first, "TestLib");
        TeaVMCRuntimeArtifacts.generate(second, "TestLib");

        Path firstAbi = first.resolve("teavmcabi");
        Path secondAbi = second.resolve("teavmcabi");
        String metadata = read(firstAbi.resolve("teavmc_abi.properties"));
        assertEquals(metadata, read(secondAbi.resolve("teavmc_abi.properties")));
        assertTrue(metadata.contains("provider-symbol=jparser_teavmc_testlib_get_api_v1"));
        assertTrue(metadata.contains("api-symbol-count=2"));

        String abi = read(firstAbi.resolve("TeaVMCAbi.h"));
        assertTrue(abi.contains("typedef void (*fp_callback)(int32_t, int64_t);"));
        assertTrue(abi.contains("typedef int32_t (*JParserTeaVMC_testlib_Fn0000)(int32_t value);"));
        assertTrue(abi.contains("typedef void (*JParserTeaVMC_testlib_Fn0001)(int64_t address, fp_callback callback);"));
        assertTrue(abi.indexOf("Fn0000") < abi.indexOf("Fn0001"));

        String dispatch = read(firstAbi.resolve("TeaVMCDispatch.h"));
        assertTrue(dispatch.contains("#define alpha(...) (jparser_teavmc_testlib_api_acquire_v1()->fn_0000)(__VA_ARGS__)"));
        assertTrue(dispatch.contains("#define zeta(...) (jparser_teavmc_testlib_api_acquire_v1()->fn_0001)(__VA_ARGS__)"));

        String source = read(firstAbi.resolve("TeaVMCDispatch.cpp"));
        assertTrue(source.contains("JPARSER_TEAVMC_REGISTER_LIBRARY_DESCRIPTOR(jparser_teavmc_testlib_descriptor_v1)"));
        assertTrue(source.contains("JPARSER_TEAVMC_LINKAGE_MODE"));
        assertTrue(source.contains("std::memory_order_release"));

        String updatedGlue = read(first.resolve("teavmcglue/TeaVMCGlue.h"));
        assertTrue(updatedGlue.contains("static const JParserTeaVMC_testlib_ApiV1 jparser_teavmc_plugin_api_v1"));
        assertTrue(updatedGlue.contains("&alpha"));
        assertTrue(updatedGlue.contains("&zeta"));
        assertTrue(updatedGlue.contains("jparser_teavmc_testlib_get_api_v1"));
    }

    @Test
    public void rejectsConflictingExportSignatures() throws Exception {
        Path root = generatedRoot("collision",
                preamble()
                        + "extern \"C\" {\n"
                        + "TEAVMC_EXPORT int32_t duplicate(int32_t value) { return value; }\n"
                        + "TEAVMC_EXPORT int64_t duplicate(int64_t value) { return value; }\n"
                        + "}\n");
        try {
            TeaVMCRuntimeArtifacts.generate(root, "Collision");
            fail("Expected a conflicting native symbol to fail generation");
        }
        catch(IOException expected) {
            assertTrue(expected.getMessage().contains("export symbol collision"));
        }
    }

    @Test
    public void callbackTypedefSignatureChangesAbiFingerprint() throws Exception {
        Path first = generatedRoot("callback-first", glueWithCallbackType("int64_t"));
        Path second = generatedRoot("callback-second", glueWithCallbackType("double"));

        TeaVMCRuntimeArtifacts.generate(first, "TestLib");
        TeaVMCRuntimeArtifacts.generate(second, "TestLib");

        String firstMetadata = read(first.resolve("teavmcabi/teavmc_abi.properties"));
        String secondMetadata = read(second.resolve("teavmcabi/teavmc_abi.properties"));
        assertEquals("1", metadataValue(firstMetadata, "api-symbol-count"));
        assertEquals("1", metadataValue(secondMetadata, "api-symbol-count"));
        assertNotEquals(metadataFingerprint(firstMetadata), metadataFingerprint(secondMetadata));
    }

    private Path generatedRoot(String name, String glue) throws Exception {
        Path root = temporaryFolder.newFolder(name).toPath();
        Path glueHeader = root.resolve("teavmcglue/TeaVMCGlue.h");
        Files.createDirectories(glueHeader.getParent());
        Files.writeString(glueHeader, glue, StandardCharsets.UTF_8);
        return root;
    }

    private static String glue(boolean reverse) {
        String alpha = "TEAVMC_EXPORT int32_t alpha(int32_t value) { return value; }\n";
        String zeta = "TEAVMC_EXPORT void zeta(int64_t address, fp_callback callback) { callback(1, address); }\n";
        return preamble() + "typedef void (*fp_callback)(int32_t, int64_t);\nextern \"C\" {\n"
                + (reverse ? zeta + alpha : alpha + zeta) + "}\n";
    }

    private static String glueWithCallbackType(String secondParameterType) {
        return preamble()
                + "typedef void (*fp_callback)(int32_t, " + secondParameterType + ");\n"
                + "extern \"C\" {\n"
                + "TEAVMC_EXPORT void invoke(fp_callback callback) { callback(1, 0); }\n"
                + "}\n";
    }

    private static String preamble() {
        return "#pragma once\n#include <cstdint>\n"
                + "#ifdef _WIN32\n#define TEAVMC_EXPORT __declspec(dllexport)\n"
                + "#else\n#define TEAVMC_EXPORT __attribute__((visibility(\"default\")))\n#endif\n";
    }

    private static String read(Path path) throws Exception {
        return Files.readString(path, StandardCharsets.UTF_8);
    }

    private static String metadataFingerprint(String metadata) {
        return metadataValue(metadata, "fingerprint-hi") + ":" + metadataValue(metadata, "fingerprint-lo");
    }

    private static String metadataValue(String metadata, String key) {
        String prefix = key + "=";
        for(String line : metadata.split("\\r?\\n")) {
            if(line.startsWith(prefix)) {
                return line.substring(prefix.length());
            }
        }
        throw new AssertionError("Missing metadata key: " + key);
    }
}
