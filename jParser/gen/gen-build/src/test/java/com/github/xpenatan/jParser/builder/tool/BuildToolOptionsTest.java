package com.github.xpenatan.jParser.builder.tool;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class BuildToolOptionsTest {

    @Test
    public void teaVMCLinkageDefaultsToStatic() {
        BuildToolOptions.BuildToolParams params = params();

        assertEquals(TeaVMCLinkage.STATIC, new BuildToolOptions(params).teaVMCLinkage);

        params.teaVMCLinkage = null;
        assertEquals(TeaVMCLinkage.STATIC, new BuildToolOptions(params).teaVMCLinkage);
    }

    @Test
    public void teaVMCLinkagePropagatesFromManualBuildParams() {
        BuildToolOptions.BuildToolParams params = params();
        params.teaVMCLinkage = TeaVMCLinkage.RUNTIME_LOADED;

        BuildToolOptions options = new BuildToolOptions(params);

        assertEquals(TeaVMCLinkage.RUNTIME_LOADED, options.teaVMCLinkage);
    }

    @Test
    public void finalClassDefaultsToTrueAndSupportsBooleanClassOverrides() {
        BuildToolOptions options = new BuildToolOptions(params());

        assertTrue(options.finalClass);

        options.setFinalClass(" DisabledLeaf ", false);
        options.setFinalClass("EnabledLeaf", true);

        assertEquals(Boolean.FALSE, options.getFinalClassOverrides().get("DisabledLeaf"));
        assertEquals(Boolean.TRUE, options.getFinalClassOverrides().get("EnabledLeaf"));
    }

    @Test
    public void finalClassOverrideRejectsBlankClassNames() {
        BuildToolOptions options = new BuildToolOptions(params());

        assertThrows(IllegalArgumentException.class, () -> options.setFinalClass(null, false));
        assertThrows(IllegalArgumentException.class, () -> options.setFinalClass("", false));
        assertThrows(IllegalArgumentException.class, () -> options.setFinalClass("   ", true));
    }

    private static BuildToolOptions.BuildToolParams params() {
        BuildToolOptions.BuildToolParams params = new BuildToolOptions.BuildToolParams();
        params.libName = "TestLib";
        params.modulePrefix = "";
        params.modulePath = ".";
        return params;
    }
}
