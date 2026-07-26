package com.github.xpenatan.jParser.builder.tool;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class JParserBuildRunnerTest {
    private static final String LINKAGE_PROPERTY = "jparser.teaVMCLinkage";
    private static final String COMPILE_FLAGS_PROPERTY = "jparser.native.windows64_teavm_c.compileFlags";
    private static final String FINAL_CLASS_PROPERTY = "jparser.finalClass";
    private static final String DISABLED_FINAL_CLASS_PROPERTY = "jparser.finalClass.DisabledLeaf";
    private static final String ENABLED_FINAL_CLASS_PROPERTY = "jparser.finalClass.EnabledLeaf";

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
    public void finalClassDefaultsToTrueAndParsesGlobalAndIndividualProperties() {
        String previousGlobal = System.getProperty(FINAL_CLASS_PROPERTY);
        String previousDisabled = System.getProperty(DISABLED_FINAL_CLASS_PROPERTY);
        String previousEnabled = System.getProperty(ENABLED_FINAL_CLASS_PROPERTY);
        try {
            System.clearProperty(FINAL_CLASS_PROPERTY);
            System.clearProperty(DISABLED_FINAL_CLASS_PROPERTY);
            System.clearProperty(ENABLED_FINAL_CLASS_PROPERTY);
            assertTrue(JParserBuildRunner.fromSystemProperties().finalClass);

            System.setProperty(FINAL_CLASS_PROPERTY, "false");
            System.setProperty(DISABLED_FINAL_CLASS_PROPERTY, "false");
            System.setProperty(ENABLED_FINAL_CLASS_PROPERTY, "true");

            JParserBuildRequest request = JParserBuildRunner.fromSystemProperties();

            assertEquals(false, request.finalClass);
            assertEquals(Boolean.FALSE, request.getFinalClassOverrides().get("DisabledLeaf"));
            assertEquals(Boolean.TRUE, request.getFinalClassOverrides().get("EnabledLeaf"));
        }
        finally {
            restoreProperty(FINAL_CLASS_PROPERTY, previousGlobal);
            restoreProperty(DISABLED_FINAL_CLASS_PROPERTY, previousDisabled);
            restoreProperty(ENABLED_FINAL_CLASS_PROPERTY, previousEnabled);
        }
    }

    @Test
    public void buildRequestFinalClassOverrideTrimsAndRejectsBlankClassNames() {
        JParserBuildRequest request = new JParserBuildRequest();

        request.setFinalClass(" Leaf ", false);

        assertEquals(Boolean.FALSE, request.getFinalClassOverrides().get("Leaf"));
        assertThrows(IllegalArgumentException.class, () -> request.setFinalClass(null, false));
        assertThrows(IllegalArgumentException.class, () -> request.setFinalClass("", false));
        assertThrows(IllegalArgumentException.class, () -> request.setFinalClass("   ", true));
    }

    private static void restoreProperty(String name, String previousValue) {
        if(previousValue == null) {
            System.clearProperty(name);
        }
        else {
            System.setProperty(name, previousValue);
        }
    }

}
