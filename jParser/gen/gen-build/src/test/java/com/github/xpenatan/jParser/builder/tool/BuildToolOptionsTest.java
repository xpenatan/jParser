package com.github.xpenatan.jParser.builder.tool;

import static org.junit.Assert.assertEquals;

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

    private static BuildToolOptions.BuildToolParams params() {
        BuildToolOptions.BuildToolParams params = new BuildToolOptions.BuildToolParams();
        params.libName = "TestLib";
        params.modulePrefix = "";
        params.modulePath = ".";
        return params;
    }
}
