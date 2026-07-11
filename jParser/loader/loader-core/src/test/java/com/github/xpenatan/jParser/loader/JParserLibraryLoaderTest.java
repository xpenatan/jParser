package com.github.xpenatan.jParser.loader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class JParserLibraryLoaderTest {

    @Test
    public void exactFileNameBypassesAllPlatformDecoration() {
        JParserLibraryLoaderOptions options = new JParserLibraryLoaderOptions();
        options.path = "native/backend";
        options.fileName = "chosen.plugin";
        options.autoAddPrefix = true;
        options.autoAddSuffix = true;

        assertEquals("native/backend/chosen.plugin",
                JParserLibraryLoader.resolveSourcePath("IgnoredLogicalName", options));
    }

    @Test
    public void legacyResolutionStillDecoratesLogicalName() {
        JParserLibraryLoaderOptions options = new JParserLibraryLoaderOptions();
        String resolved = JParserLibraryLoader.resolveSourcePath("CompatibilityName", options);

        assertTrue(resolved.contains("CompatibilityName"));
        assertFalse(resolved.isEmpty());
    }
}
