package com.github.xpenatan.jParser.builder.tool;

import static org.junit.Assert.assertEquals;

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

}
