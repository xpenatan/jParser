package com.github.xpenatan.jParser.builder.tool;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import java.util.Properties;
import org.junit.Test;

public class JParserBuildRunnerTest {
    private static final String LINKAGE_PROPERTY = "jparser.teaVMCLinkage";
    private static final String COMPILE_FLAGS_PROPERTY = "jparser.native.windows64_teavm_c.compileFlags";

    @Test
    public void teaVMCLinkageDefaultsToStaticAndParsesSystemProperty() {
        String previous = System.getProperty(LINKAGE_PROPERTY);
        try {
            System.clearProperty(LINKAGE_PROPERTY);
            assertEquals(TeaVMCLinkage.STATIC,
                    JParserBuildRunner.fromSystemProperties().params.teaVMCLinkage);

            System.setProperty(LINKAGE_PROPERTY, "shared_linked");
            assertEquals(TeaVMCLinkage.SHARED_LINKED,
                    JParserBuildRunner.fromSystemProperties().params.teaVMCLinkage);
        }
        finally {
            if(previous == null) {
                System.clearProperty(LINKAGE_PROPERTY);
            }
            else {
                System.setProperty(LINKAGE_PROPERTY, previous);
            }
        }
    }

    @Test
    public void targetCompileFlagsParseWithoutPlatformSpecificConfiguration() {
        String previous = System.getProperty(COMPILE_FLAGS_PROPERTY);
        String previousTargets = System.getProperty("jparser.native.targets");
        try {
            System.setProperty("jparser.native.targets", "windows64_teavm_c");
            System.setProperty(COMPILE_FLAGS_PROPERTY, "/MD");
            assertEquals(java.util.List.of("/MD"), JParserBuildRunner.fromSystemProperties()
                    .targetConfig.target("windows64_teavm_c").compileFlags);
        }
        finally {
            if(previous == null) {
                System.clearProperty(COMPILE_FLAGS_PROPERTY);
            }
            else {
                System.setProperty(COMPILE_FLAGS_PROPERTY, previous);
            }
            if(previousTargets == null) {
                System.clearProperty("jparser.native.targets");
            }
            else {
                System.setProperty("jparser.native.targets", previousTargets);
            }
        }
    }

    @Test
    public void parsesGradlePluginRequestProtocol() {
        Properties properties = new Properties();
        properties.setProperty("jparser.generateCore", "false");
        properties.setProperty("jparser.libName", "sample");
        properties.setProperty("jparser.idlName", "");
        properties.setProperty("jparser.modulePrefix", "");
        properties.setProperty("jparser.webExportedFunctions", "_malloc\n_free");
        properties.setProperty("jparser.additionalJavaClassPaths", "classes/a\nclasses/b");
        properties.setProperty("jparser.native.targets", "windows64_teavm_c");
        properties.setProperty(
            "jparser.native.windows64_teavm_c.webExportedRuntimeMethods",
            "ccall\ncwrap"
        );
        properties.setProperty("jparser.teaVMCConsumers.count", "1");
        properties.setProperty(
            "jparser.teaVMCConsumers.0.targetName",
            "windows64_teavm_c"
        );
        properties.setProperty("jparser.teaVMCConsumers.0.variantName", "wgpu");
        properties.setProperty(
            "jparser.teaVMCConsumers.0.staticLibraries.count",
            "1"
        );
        properties.setProperty(
            "jparser.teaVMCConsumers.0.staticLibraries.0.resourcePath",
            "lib/wgpu.lib"
        );
        properties.setProperty(
            "jparser.teaVMCConsumers.0.staticLibraries.0.overrideVariable",
            "WGPU_LIBRARY"
        );

        JParserBuildRequest request = JParserBuildRunner.fromProperties(properties);

        assertFalse(request.generateCore);
        assertNull(request.params.idlName);
        assertEquals("", request.params.modulePrefix);
        assertEquals(
            java.util.List.of("_malloc", "_free"),
            request.targetConfig.webExportedFunctions
        );
        assertEquals(
            java.util.List.of("classes/a", "classes/b"),
            request.additionalJavaClassPaths
        );
        assertEquals(
            java.util.List.of("ccall", "cwrap"),
            request.targetConfig.target("windows64_teavm_c").webExportedRuntimeMethods
        );
        assertEquals(1, request.teaVMCConsumers.size());
        TeaVMCConsumerConfig consumer = request.teaVMCConsumers.get(0);
        assertEquals("wgpu", consumer.variantName);
        assertEquals("lib/wgpu.lib", consumer.staticLibraries.get(0).resourcePath);
        assertEquals("WGPU_LIBRARY", consumer.staticLibraries.get(0).overrideVariable);
    }
}
