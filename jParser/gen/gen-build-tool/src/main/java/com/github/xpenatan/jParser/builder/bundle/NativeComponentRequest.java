package com.github.xpenatan.jParser.builder.bundle;

import java.nio.file.Path;
import java.util.ArrayList;

public final class NativeComponentRequest {
    public Path outputJar;
    public String componentId;
    public String componentVersion;
    public String variantName = "";
    public NativeComponentRole role = NativeComponentRole.BINDING;
    public NativeTarget target;
    public NativeBridge bridge;
    public NativeBuildType buildType = NativeBuildType.RELEASE;
    public int minimumJavaVersion = 8;
    public String minimumPlatformVersion = "";
    public String runtimeAbi;
    public String toolchainId;
    public String toolchainVersion = "";
    public String cRuntime = "";
    public String cppRuntime = "";
    public String webModuleName = "";
    public Path implementationArchive;
    public Path bridgeArchive;
    public final ArrayList<NativeArchiveInput> dependencyArchives = new ArrayList<>();
    public final ArrayList<String> systemLibraries = new ArrayList<>();
    public final ArrayList<String> frameworks = new ArrayList<>();
    public final ArrayList<String> dynamicDependencies = new ArrayList<>();
    public final ArrayList<String> linkerOptions = new ArrayList<>();
    public final ArrayList<String> exportedSymbols = new ArrayList<>();
    public final ArrayList<Path> webIDLFiles = new ArrayList<>();
    public final ArrayList<Path> webHeaderDirectories = new ArrayList<>();
    public final ArrayList<Path> licenseFiles = new ArrayList<>();

    public String classifier() {
        return NativeResourceClassifier.of(target, bridge, normalizedVariant());
    }

    String normalizedVariant() {
        if(variantName == null || variantName.trim().isEmpty()) {
            return "";
        }
        return NativeTarget.requireSegment("variantName", variantName);
    }
}
