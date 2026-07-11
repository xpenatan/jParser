package com.github.xpenatan.jParser.loader;

public class JParserLibraryLoaderOptions {
    public boolean autoAddPrefix = true;
    public boolean autoAddSuffix = true;
    public String path;
    /**
     * Exact physical native-library filename. When set, platform prefix and
     * suffix decoration is skipped while {@link #path} remains the containing
     * directory.
     */
    public String fileName;
}
