package com.github.xpenatan.jParser.builder.tool;

import java.util.ArrayList;

/**
 * Portable native requirements contributed to a final TeaVM C application.
 *
 * <p>Paths are relative to the library's packaged native platform directory.
 * They must not refer to producer-machine files.</p>
 */
public class TeaVMCConsumerConfig {
    public String targetName;
    public String variantName;
    public final ArrayList<String> selectorResources = new ArrayList<>();
    public final ArrayList<String> headerDirs = new ArrayList<>();
    public final ArrayList<String> compileDefinitions = new ArrayList<>();
    public final ArrayList<String> compileOptions = new ArrayList<>();
    public final ArrayList<StaticLibrary> staticLibraries = new ArrayList<>();
    public final ArrayList<String> staticLinkLibraries = new ArrayList<>();
    public final ArrayList<String> staticLinkOptions = new ArrayList<>();

    public static class StaticLibrary {
        public String resourcePath;
        public String overrideVariable;

        public StaticLibrary() {
        }

        public StaticLibrary(String resourcePath, String overrideVariable) {
            this.resourcePath = resourcePath;
            this.overrideVariable = overrideVariable;
        }
    }
}
