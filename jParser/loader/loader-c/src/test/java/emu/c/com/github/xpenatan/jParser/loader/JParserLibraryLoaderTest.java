package emu.c.com.github.xpenatan.jParser.loader;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.github.xpenatan.jParser.loader.JParserLibraryLoaderListener;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;

public class JParserLibraryLoaderTest {

    @Test
    public void cStringsUseUtf8AndTerminateWithNull() {
        assertArrayEquals(new byte[] { 0x47, 0x50, 0x55, (byte)0xE2, (byte)0x98,
                        (byte)0x83, 0 },
                JParserLibraryLoader.toCString("GPU\u2603"));
        assertNull(JParserLibraryLoader.toCString(null));
    }

    @Test
    public void listenerIsInvokedExactlyOnceWhenItThrows() {
        AtomicInteger calls = new AtomicInteger();
        JParserLibraryLoaderListener listener = (success, failure) -> {
            calls.incrementAndGet();
            throw new IllegalStateException("listener failure");
        };

        try {
            JParserLibraryLoader.notifyListener(listener, null);
            fail("Expected listener failure");
        }
        catch(IllegalStateException expected) {
            assertEquals("listener failure", expected.getMessage());
        }
        assertEquals(1, calls.get());
    }

    @Test
    public void failureIsDeliveredWithoutWrappingOrASecondCallback() {
        AtomicInteger calls = new AtomicInteger();
        RuntimeException failure = new RuntimeException("native failure");
        JParserLibraryLoaderListener listener = (success, delivered) -> {
            calls.incrementAndGet();
            assertFalse(success);
            assertSame(failure, delivered);
        };

        JParserLibraryLoader.notifyListener(listener, failure);
        assertEquals(1, calls.get());
    }

    @Test
    public void nativeHeaderExposesStablePluginAndDescriptorAbi() throws IOException {
        String header = readResource("external_cpp/jparser/loader/teavmc_loader.h");

        assertTrue(header.contains("JPARSER_TEAVMC_PLUGIN_API_MAGIC"));
        assertTrue(header.contains("JPARSER_TEAVMC_PLUGIN_ABI_MAJOR"));
        assertTrue(header.contains("typedef struct JParserTeaVMCPluginApiHeader"));
        assertTrue(header.contains("uint64_t fingerprint_hi;"));
        assertTrue(header.contains("uint64_t fingerprint_lo;"));
        assertTrue(header.contains("typedef struct JParserTeaVMCLibraryDescriptor"));
        assertTrue(header.contains("uint32_t linkage_mode;"));
        assertTrue(header.contains("JParserTeaVMCBindApiFn bind_api;"));
        assertTrue(header.contains("JPARSER_TEAVMC_REGISTER_LIBRARY_DESCRIPTOR"));
    }

    @Test
    public void nativeRuntimeContainsEverySupportedLoaderBranchAndNoUnloadApi() throws IOException {
        String source = readResource("external_cpp/jparser/loader/teavmc_loader.cpp");

        assertTrue(source.contains("LoadLibraryExW"));
        assertTrue(source.contains("GetProcAddress"));
        assertTrue(source.contains("dlopen(path.c_str(), RTLD_NOW | RTLD_LOCAL)"));
        assertTrue(source.contains("dlsym(handle, symbol)"));
        assertTrue(source.contains("#if defined(__APPLE__)"));
        assertTrue(source.contains("return joinPath(\"@loader_path\", file);"));
        assertTrue(source.contains("JPARSER_TEAVMC_LINKAGE_STATIC"));
        assertTrue(source.contains("JPARSER_TEAVMC_LINKAGE_SHARED_LINKED"));
        assertTrue(source.contains("JPARSER_TEAVMC_LINKAGE_RUNTIME_LOADED"));
        assertFalse(source.contains("jparser_teavmc_loader_unload"));
    }

    @Test
    public void portableHookAddsOneCxx17LoaderSourceAndDlSupport() throws IOException {
        String cmake = readResource("external_cpp/cmake/post_target/jparser_00_teavmc_loader.cmake");

        assertTrue(cmake.contains("target_sources(${JPARSER_TEAVMC_APP_TARGET}"));
        assertTrue(cmake.contains("cxx_std_17"));
        assertTrue(cmake.contains("${CMAKE_DL_LIBS}"));
        assertTrue(cmake.contains("PROPERTY JPARSER_TEAVMC_LOADER_ADDED"));
        assertTrue(cmake.contains("JPARSER_TEAVMC_GENERATED_SOURCE_ROOT"));
        assertTrue(cmake.contains("PROPERTY COMPILE_OPTIONS \"/FI${JPARSER_TEAVMC_LOADER_HEADER}\""));
        assertTrue(cmake.contains("PROPERTY COMPILE_OPTIONS \"-include\" \"${JPARSER_TEAVMC_LOADER_HEADER}\""));
    }

    private static String readResource(String path) throws IOException {
        try(InputStream input = JParserLibraryLoaderTest.class.getClassLoader().getResourceAsStream(path)) {
            if(input == null) {
                throw new IOException("Missing test resource: " + path);
            }
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
