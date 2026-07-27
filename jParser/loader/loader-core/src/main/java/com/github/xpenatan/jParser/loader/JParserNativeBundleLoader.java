package com.github.xpenatan.jParser.loader;

/**
 * Entry point for loading a fat native bundle.
 *
 * <p>A fat-mode application calls this loader once and must not invoke the generated
 * per-library loaders for components already linked into the bundle. Standalone component
 * loading remains available through {@link JParserLibraryLoader}.</p>
 */
public final class JParserNativeBundleLoader {
    private JParserNativeBundleLoader() {
    }

    public static void load(String bundleName, JParserLibraryLoaderListener listener) {
        JParserLibraryLoader.load(bundleName, listener);
    }

    public static void load(
            String bundleName,
            JParserLibraryLoaderOptions options,
            JParserLibraryLoaderListener listener) {
        JParserLibraryLoader.load(bundleName, options, listener);
    }

}
