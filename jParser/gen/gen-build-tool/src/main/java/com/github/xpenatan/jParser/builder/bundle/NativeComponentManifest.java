package com.github.xpenatan.jParser.builder.bundle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class NativeComponentManifest {
    public static final int FORMAT_VERSION = 1;
    public static final String PATH = "META-INF/jparser/native-component.properties";

    private final String componentId;
    private final String componentVersion;
    private final String variantName;
    private final NativeComponentRole role;
    private final NativeTarget target;
    private final NativeBridge bridge;
    private final NativeBuildType buildType;
    private final int minimumJavaVersion;
    private final String minimumPlatformVersion;
    private final String runtimeAbi;
    private final String toolchainId;
    private final String toolchainVersion;
    private final String cRuntime;
    private final String cppRuntime;
    private final String classifier;
    private final String webModuleName;
    private final List<String> systemLibraries;
    private final List<String> frameworks;
    private final List<String> linkerOptions;
    private final List<String> exportedSymbols;
    private final List<FileEntry> files;

    NativeComponentManifest(
            String componentId,
            String componentVersion,
            String variantName,
            NativeComponentRole role,
            NativeTarget target,
            NativeBridge bridge,
            NativeBuildType buildType,
            int minimumJavaVersion,
            String minimumPlatformVersion,
            String runtimeAbi,
            String toolchainId,
            String toolchainVersion,
            String cRuntime,
            String cppRuntime,
            String classifier,
            String webModuleName,
            List<String> systemLibraries,
            List<String> frameworks,
            List<String> linkerOptions,
            List<String> exportedSymbols,
            List<FileEntry> files) {
        this.componentId = componentId;
        this.componentVersion = componentVersion;
        this.variantName = variantName;
        this.role = role;
        this.target = target;
        this.bridge = bridge;
        this.buildType = buildType;
        this.minimumJavaVersion = minimumJavaVersion;
        this.minimumPlatformVersion = minimumPlatformVersion;
        this.runtimeAbi = runtimeAbi;
        this.toolchainId = toolchainId;
        this.toolchainVersion = toolchainVersion;
        this.cRuntime = cRuntime;
        this.cppRuntime = cppRuntime;
        this.classifier = classifier;
        this.webModuleName = webModuleName;
        this.systemLibraries = immutable(systemLibraries);
        this.frameworks = immutable(frameworks);
        this.linkerOptions = immutable(linkerOptions);
        this.exportedSymbols = immutable(exportedSymbols);
        this.files = Collections.unmodifiableList(new ArrayList<>(files));
    }

    public String getComponentId() {
        return componentId;
    }

    public String getComponentVersion() {
        return componentVersion;
    }

    public String getVariantName() {
        return variantName;
    }

    public NativeComponentRole getRole() {
        return role;
    }

    public NativeTarget getTarget() {
        return target;
    }

    public NativeBridge getBridge() {
        return bridge;
    }

    public NativeBuildType getBuildType() {
        return buildType;
    }

    public int getMinimumJavaVersion() {
        return minimumJavaVersion;
    }

    public String getMinimumPlatformVersion() {
        return minimumPlatformVersion;
    }

    public String getRuntimeAbi() {
        return runtimeAbi;
    }

    public String getToolchainId() {
        return toolchainId;
    }

    public String getToolchainVersion() {
        return toolchainVersion;
    }

    public String getCRuntime() {
        return cRuntime;
    }

    public String getCppRuntime() {
        return cppRuntime;
    }

    public String getClassifier() {
        return classifier;
    }

    public String getWebModuleName() {
        return webModuleName;
    }

    public List<String> getSystemLibraries() {
        return systemLibraries;
    }

    public List<String> getFrameworks() {
        return frameworks;
    }

    public List<String> getLinkerOptions() {
        return linkerOptions;
    }

    public List<String> getExportedSymbols() {
        return exportedSymbols;
    }

    public List<FileEntry> getFiles() {
        return files;
    }

    FileEntry requireFile(NativeComponentFileRole role) {
        FileEntry result = null;
        for(FileEntry file : files) {
            if(file.role == role) {
                if(result != null) {
                    throw new IllegalStateException("Component has multiple " + role + " files");
                }
                result = file;
            }
        }
        if(result == null) {
            throw new IllegalStateException("Component is missing " + role + " file");
        }
        return result;
    }

    private static List<String> immutable(List<String> values) {
        return Collections.unmodifiableList(new ArrayList<>(values));
    }

    public static final class FileEntry {
        private final NativeComponentFileRole role;
        private final String name;
        private final String path;
        private final String sha256;
        private final NativeArchiveLinkMode linkMode;

        FileEntry(NativeComponentFileRole role, String name, String path, String sha256,
                  NativeArchiveLinkMode linkMode) {
            this.role = role;
            this.name = name;
            this.path = path;
            this.sha256 = sha256;
            this.linkMode = linkMode;
        }

        public String getRole() {
            return role.name();
        }

        NativeComponentFileRole role() {
            return role;
        }

        public String getName() {
            return name;
        }

        public String getPath() {
            return path;
        }

        public String getSha256() {
            return sha256;
        }

        public NativeArchiveLinkMode getLinkMode() {
            return linkMode;
        }
    }
}
