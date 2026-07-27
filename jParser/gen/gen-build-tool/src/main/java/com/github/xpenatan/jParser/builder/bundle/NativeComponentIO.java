package com.github.xpenatan.jParser.builder.bundle;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

final class NativeComponentIO {
    private NativeComponentIO() {
    }

    static String sha256(Path path) throws IOException {
        try(InputStream input = Files.newInputStream(path)) {
            return sha256(input);
        }
    }

    static String sha256(InputStream input) throws IOException {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        }
        catch(NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
        byte[] buffer = new byte[64 * 1024];
        int read;
        while((read = input.read(buffer)) != -1) {
            digest.update(buffer, 0, read);
        }
        byte[] hash = digest.digest();
        StringBuilder result = new StringBuilder(hash.length * 2);
        for(byte value : hash) {
            result.append(Character.forDigit((value >>> 4) & 0xf, 16));
            result.append(Character.forDigit(value & 0xf, 16));
        }
        return result.toString();
    }

    static byte[] writeManifest(NativeComponentManifest manifest) {
        TreeMap<String, String> values = new TreeMap<>();
        values.put("format.version", Integer.toString(NativeComponentManifest.FORMAT_VERSION));
        values.put("component.id", manifest.getComponentId());
        values.put("component.version", manifest.getComponentVersion());
        values.put("component.variant", manifest.getVariantName());
        values.put("component.role", manifest.getRole().name());
        values.put("component.classifier", manifest.getClassifier());
        values.put("target.os", manifest.getTarget().getOperatingSystem().getId());
        values.put("target.architecture", manifest.getTarget().getArchitecture().getId());
        values.put("target.abi", manifest.getTarget().getAbi());
        values.put("target.environment", manifest.getTarget().getEnvironment());
        values.put("bridge", manifest.getBridge().getId());
        values.put("build.type", manifest.getBuildType().name());
        values.put("java.minimum", Integer.toString(manifest.getMinimumJavaVersion()));
        values.put("target.minimumVersion", manifest.getMinimumPlatformVersion());
        values.put("runtime.abi", manifest.getRuntimeAbi());
        values.put("toolchain.id", manifest.getToolchainId());
        values.put("toolchain.version", manifest.getToolchainVersion());
        values.put("toolchain.cRuntime", manifest.getCRuntime());
        values.put("toolchain.cppRuntime", manifest.getCppRuntime());
        values.put("web.moduleName", manifest.getWebModuleName());
        addList(values, "systemLibrary", manifest.getSystemLibraries());
        addList(values, "framework", manifest.getFrameworks());
        addList(values, "linkerOption", manifest.getLinkerOptions());
        addList(values, "export", manifest.getExportedSymbols());
        values.put("file.count", Integer.toString(manifest.getFiles().size()));
        for(int i = 0; i < manifest.getFiles().size(); i++) {
            NativeComponentManifest.FileEntry file = manifest.getFiles().get(i);
            String prefix = "file." + i + ".";
            values.put(prefix + "role", file.getRole());
            values.put(prefix + "name", file.getName());
            values.put(prefix + "path", file.getPath());
            values.put(prefix + "sha256", file.getSha256());
            values.put(prefix + "linkMode", file.getLinkMode() == null ? "" : file.getLinkMode().name());
        }

        StringBuilder output = new StringBuilder();
        for(Map.Entry<String, String> entry : values.entrySet()) {
            output.append(escape(entry.getKey(), true))
                    .append('=')
                    .append(escape(entry.getValue(), false))
                    .append('\n');
        }
        return output.toString().getBytes(StandardCharsets.UTF_8);
    }

    static NativeComponentManifest readManifest(InputStream input) throws IOException {
        Properties properties = new Properties();
        properties.load(new java.io.InputStreamReader(input, StandardCharsets.UTF_8));
        int formatVersion = integer(properties, "format.version");
        if(formatVersion != NativeComponentManifest.FORMAT_VERSION) {
            throw new IllegalArgumentException("Unsupported native component format version: " + formatVersion);
        }

        String componentId = required(properties, "component.id");
        String componentVersion = required(properties, "component.version");
        String variantName = optional(properties, "component.variant");
        NativeComponentRole role = enumValue(NativeComponentRole.class, required(properties, "component.role"),
                "component.role");
        String classifier = required(properties, "component.classifier");
        NativeTarget target = NativeTarget.fromManifest(
                required(properties, "target.os"),
                required(properties, "target.architecture"),
                optional(properties, "target.abi"),
                optional(properties, "target.environment"));
        NativeBridge bridge = NativeBridge.fromId(required(properties, "bridge"));
        NativeBuildType buildType = enumValue(NativeBuildType.class, required(properties, "build.type"),
                "build.type");
        int minimumJavaVersion = integer(properties, "java.minimum");
        String minimumPlatformVersion = optional(properties, "target.minimumVersion");
        String runtimeAbi = required(properties, "runtime.abi");
        String toolchainId = required(properties, "toolchain.id");
        String toolchainVersion = optional(properties, "toolchain.version");
        String cRuntime = optional(properties, "toolchain.cRuntime");
        String cppRuntime = optional(properties, "toolchain.cppRuntime");
        String webModuleName = optional(properties, "web.moduleName");

        ArrayList<NativeComponentManifest.FileEntry> files = new ArrayList<>();
        int fileCount = count(properties, "file.count");
        for(int i = 0; i < fileCount; i++) {
            String prefix = "file." + i + ".";
            NativeComponentFileRole fileRole = enumValue(NativeComponentFileRole.class,
                    required(properties, prefix + "role"), prefix + "role");
            String linkModeValue = optional(properties, prefix + "linkMode");
            NativeArchiveLinkMode linkMode = linkModeValue.isEmpty()
                    ? null
                    : enumValue(NativeArchiveLinkMode.class, linkModeValue, prefix + "linkMode");
            files.add(new NativeComponentManifest.FileEntry(
                    fileRole,
                    required(properties, prefix + "name"),
                    required(properties, prefix + "path"),
                    required(properties, prefix + "sha256"),
                    linkMode));
        }

        NativeComponentManifest manifest = new NativeComponentManifest(
                componentId,
                componentVersion,
                variantName,
                role,
                target,
                bridge,
                buildType,
                minimumJavaVersion,
                minimumPlatformVersion,
                runtimeAbi,
                toolchainId,
                toolchainVersion,
                cRuntime,
                cppRuntime,
                classifier,
                webModuleName,
                list(properties, "systemLibrary"),
                list(properties, "framework"),
                list(properties, "linkerOption"),
                list(properties, "export"),
                files);
        validateManifest(manifest);
        return manifest;
    }

    static void validateManifest(NativeComponentManifest manifest) {
        validateMinimumPlatformVersion(manifest);
        String expectedClassifier = manifest.getTarget().getClassifierPrefix();
        if(manifest.getTarget().getOperatingSystem() != NativeTarget.OperatingSystem.WEB) {
            expectedClassifier += "-" + manifest.getBridge().getId();
        }
        if(!manifest.getVariantName().isEmpty()) {
            expectedClassifier += "-" + NativeTarget.requireSegment("component.variant", manifest.getVariantName());
        }
        if(!expectedClassifier.equals(manifest.getClassifier())) {
            throw new IllegalArgumentException("Native component classifier mismatch: expected "
                    + expectedClassifier + " but found " + manifest.getClassifier());
        }

        int implementationCount = 0;
        int bridgeCount = 0;
        ArrayList<String> paths = new ArrayList<>();
        for(NativeComponentManifest.FileEntry file : manifest.getFiles()) {
            requireSafeEntryPath(file.getPath());
            if(paths.contains(file.getPath())) {
                throw new IllegalArgumentException("Duplicate native component path: " + file.getPath());
            }
            paths.add(file.getPath());
            if(!file.getSha256().matches("[0-9a-f]{64}")) {
                throw new IllegalArgumentException("Invalid SHA-256 for native component path: " + file.getPath());
            }
            String requiredPrefix;
            switch(file.role()) {
                case IMPLEMENTATION:
                    implementationCount++;
                    requiredPrefix = "native/implementation/";
                    requireLinkMode(file);
                    break;
                case BRIDGE:
                    bridgeCount++;
                    requiredPrefix = "native/bridge/";
                    requireLinkMode(file);
                    break;
                case DEPENDENCY:
                    requiredPrefix = "native/dependencies/";
                    requireLinkMode(file);
                    break;
                case WEB_IDL:
                    requiredPrefix = "web/idl/";
                    requireNoLinkMode(file);
                    break;
                case WEB_HEADER:
                    requiredPrefix = "web/include/";
                    requireNoLinkMode(file);
                    break;
                case LICENSE:
                    requiredPrefix = "META-INF/licenses/";
                    requireNoLinkMode(file);
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported native component file role: " + file.getRole());
            }
            if(!file.getPath().startsWith(requiredPrefix)) {
                throw new IllegalArgumentException(file.getRole() + " path must be below " + requiredPrefix
                        + ": " + file.getPath());
            }
        }
        if(implementationCount != 1) {
            throw new IllegalArgumentException("Native component must contain exactly one implementation archive");
        }
        boolean web = manifest.getTarget().getOperatingSystem() == NativeTarget.OperatingSystem.WEB;
        if(web && bridgeCount != 0) {
            throw new IllegalArgumentException("Web native component must generate its merged bridge at bundle time");
        }
        if(!web && bridgeCount != 1) {
            throw new IllegalArgumentException("Native component must contain exactly one bridge archive");
        }
    }

    private static void validateMinimumPlatformVersion(NativeComponentManifest manifest) {
        String value = manifest.getMinimumPlatformVersion();
        if(value.isEmpty()) {
            return;
        }
        switch(manifest.getTarget().getOperatingSystem()) {
            case ANDROID:
                try {
                    int api = Integer.parseInt(value);
                    if(api < 21 || api > 999) {
                        throw new IllegalArgumentException(
                                "Android native component minimum platform version must be between 21 and 999: "
                                        + value);
                    }
                }
                catch(NumberFormatException exception) {
                    throw new IllegalArgumentException(
                            "Android native component minimum platform version must be a numeric API level: "
                                    + value,
                            exception);
                }
                break;
            case MACOS:
            case IOS:
                if(!value.matches("[0-9]+(?:\\.[0-9]+){0,3}")) {
                    throw new IllegalArgumentException(
                            manifest.getTarget().getOperatingSystem().getId()
                                    + " native component minimum platform version must be numeric and dotted: "
                                    + value);
                }
                break;
            default:
                if(value.indexOf('\0') >= 0 || value.indexOf('\n') >= 0 || value.indexOf('\r') >= 0) {
                    throw new IllegalArgumentException(
                            "Native component minimum platform version contains a control character");
                }
                break;
        }
    }

    static void requireSafeEntryPath(String entryPath) {
        if(entryPath == null || entryPath.isEmpty()
                || entryPath.startsWith("/")
                || entryPath.startsWith("\\")
                || entryPath.contains("\\")
                || entryPath.indexOf('\0') >= 0) {
            throw new IllegalArgumentException("Unsafe native component entry path: " + entryPath);
        }
        Path normalized = Path.of(entryPath).normalize();
        if(normalized.isAbsolute() || normalized.startsWith("..") || !normalized.toString().replace('\\', '/').equals(entryPath)) {
            throw new IllegalArgumentException("Unsafe native component entry path: " + entryPath);
        }
    }

    static byte[] readLimited(InputStream input, int maximumBytes, String description) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int read;
        while((read = input.read(buffer)) != -1) {
            if(output.size() + read > maximumBytes) {
                throw new IllegalArgumentException(description + " exceeds " + maximumBytes + " bytes");
            }
            output.write(buffer, 0, read);
        }
        return output.toByteArray();
    }

    private static void addList(TreeMap<String, String> values, String prefix, List<String> entries) {
        values.put(prefix + ".count", Integer.toString(entries.size()));
        for(int i = 0; i < entries.size(); i++) {
            values.put(prefix + "." + i, entries.get(i));
        }
    }

    private static List<String> list(Properties properties, String prefix) {
        int count = count(properties, prefix + ".count");
        ArrayList<String> values = new ArrayList<>();
        for(int i = 0; i < count; i++) {
            values.add(required(properties, prefix + "." + i));
        }
        return Collections.unmodifiableList(values);
    }

    private static int count(Properties properties, String name) {
        int value = integer(properties, name);
        if(value < 0 || value > 100_000) {
            throw new IllegalArgumentException("Invalid native component count " + name + ": " + value);
        }
        return value;
    }

    private static int integer(Properties properties, String name) {
        String value = required(properties, name);
        try {
            return Integer.parseInt(value);
        }
        catch(NumberFormatException exception) {
            throw new IllegalArgumentException("Native component property " + name + " must be an integer", exception);
        }
    }

    private static String required(Properties properties, String name) {
        String value = properties.getProperty(name);
        if(value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Missing native component property: " + name);
        }
        return value.trim();
    }

    private static String optional(Properties properties, String name) {
        String value = properties.getProperty(name);
        return value == null ? "" : value.trim();
    }

    private static <T extends Enum<T>> T enumValue(Class<T> type, String value, String name) {
        try {
            return Enum.valueOf(type, value);
        }
        catch(IllegalArgumentException exception) {
            throw new IllegalArgumentException("Invalid native component property " + name + ": " + value,
                    exception);
        }
    }

    private static void requireLinkMode(NativeComponentManifest.FileEntry file) {
        if(file.getLinkMode() == null) {
            throw new IllegalArgumentException(file.getRole() + " file is missing link mode: " + file.getPath());
        }
    }

    private static void requireNoLinkMode(NativeComponentManifest.FileEntry file) {
        if(file.getLinkMode() != null) {
            throw new IllegalArgumentException(file.getRole() + " file cannot have a link mode: " + file.getPath());
        }
    }

    private static String escape(String value, boolean key) {
        StringBuilder escaped = new StringBuilder();
        for(int i = 0; i < value.length(); i++) {
            char character = value.charAt(i);
            switch(character) {
                case '\\':
                    escaped.append("\\\\");
                    break;
                case '\t':
                    escaped.append("\\t");
                    break;
                case '\n':
                    escaped.append("\\n");
                    break;
                case '\r':
                    escaped.append("\\r");
                    break;
                case '=':
                case ':':
                    escaped.append('\\').append(character);
                    break;
                case '#':
                case '!':
                    if(key || i == 0) {
                        escaped.append('\\');
                    }
                    escaped.append(character);
                    break;
                case ' ':
                    if(i == 0 || key) {
                        escaped.append('\\');
                    }
                    escaped.append(' ');
                    break;
                default:
                    escaped.append(character);
                    break;
            }
        }
        return escaped.toString();
    }
}
