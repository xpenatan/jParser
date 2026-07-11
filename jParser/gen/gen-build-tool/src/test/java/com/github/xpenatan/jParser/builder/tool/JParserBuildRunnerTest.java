package com.github.xpenatan.jParser.builder.tool;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class JParserBuildRunnerTest {
    private static final String LINKAGE_PROPERTY = "jparser.teaVMCLinkage";

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
}
