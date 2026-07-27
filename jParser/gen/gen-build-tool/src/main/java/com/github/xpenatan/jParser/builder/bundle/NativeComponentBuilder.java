package com.github.xpenatan.jParser.builder.bundle;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public final class NativeComponentBuilder {
    private static final FileTime REPRODUCIBLE_TIMESTAMP = FileTime.fromMillis(0L);

    private NativeComponentBuilder() {
    }

    public static NativeComponentArtifact build(NativeComponentRequest request) throws IOException {
        validate(request);
        ArrayList<SourceEntry> sources = collectSources(request);
        ArrayList<NativeComponentManifest.FileEntry> files = new ArrayList<>();
        for(SourceEntry source : sources) {
            files.add(new NativeComponentManifest.FileEntry(
                    source.role,
                    source.name,
                    source.entryPath,
                    NativeComponentIO.sha256(source.sourcePath),
                    source.linkMode));
        }

        NativeComponentManifest manifest = new NativeComponentManifest(
                request.componentId.trim(),
                request.componentVersion.trim(),
                request.normalizedVariant(),
                request.role,
                request.target,
                request.bridge,
                request.buildType,
                request.minimumJavaVersion,
                normalizedPlatformVersion(request),
                request.runtimeAbi.trim(),
                request.toolchainId.trim(),
                optional(request.toolchainVersion),
                optional(request.cRuntime),
                optional(request.cppRuntime),
                request.classifier(),
                optional(request.webModuleName),
                normalizedUniqueList(request.systemLibraries, "system library", true),
                normalizedUniqueList(request.frameworks, "framework", true),
                normalizedUniqueList(request.linkerOptions, "linker option", false),
                normalizedUniqueList(request.exportedSymbols, "exported symbol", false),
                files);
        NativeComponentIO.validateManifest(manifest);

        Path output = request.outputJar.toAbsolutePath().normalize();
        Path parent = output.getParent();
        if(parent == null) {
            throw new IllegalArgumentException("Native component output requires a parent directory: " + output);
        }
        Files.createDirectories(parent);
        Path temporary = Files.createTempFile(parent, output.getFileName().toString(), ".tmp");
        boolean succeeded = false;
        try {
            writeJar(temporary, manifest, sources);
            moveReplacing(temporary, output);
            succeeded = true;
        }
        finally {
            if(!succeeded) {
                Files.deleteIfExists(temporary);
            }
        }
        NativeComponentManifest verified = NativeComponentReader.read(output);
        return new NativeComponentArtifact(output, request.classifier(), verified);
    }

    private static void validate(NativeComponentRequest request) throws IOException {
        if(request == null) {
            throw new IllegalArgumentException("Native component request must not be null");
        }
        if(request.outputJar == null) {
            throw new IllegalArgumentException("Native component outputJar is required");
        }
        requireIdentifier("componentId", request.componentId);
        requireValue("componentVersion", request.componentVersion);
        if(request.role == null || request.target == null || request.bridge == null || request.buildType == null) {
            throw new IllegalArgumentException("Native component role, target, bridge, and buildType are required");
        }
        requireValue("runtimeAbi", request.runtimeAbi);
        requireIdentifier("toolchainId", request.toolchainId);
        if(request.minimumJavaVersion < 8) {
            throw new IllegalArgumentException("Native component minimum Java version must be at least 8");
        }
        if(request.bridge == NativeBridge.FFM && request.minimumJavaVersion < 25) {
            throw new IllegalArgumentException("FFM native components require minimum Java version 25");
        }
        normalizedPlatformVersion(request);
        validateTargetBridge(request.target, request.bridge);
        NativeArchiveInspector.requireStaticArchive(request.implementationArchive, "Implementation archive");

        boolean web = request.target.getOperatingSystem() == NativeTarget.OperatingSystem.WEB;
        if(web) {
            if(request.bridgeArchive != null) {
                throw new IllegalArgumentException("Web component cannot contain a precompiled bridge archive");
            }
            requireIdentifier("webModuleName", request.webModuleName);
            if(request.webIDLFiles.isEmpty()) {
                throw new IllegalArgumentException("Web component requires at least one owned WebIDL file");
            }
            if(request.webHeaderDirectories.isEmpty()) {
                throw new IllegalArgumentException("Web component requires at least one header directory");
            }
        }
        else {
            NativeArchiveInspector.requireStaticArchive(request.bridgeArchive, "Bridge archive");
            if(!request.webIDLFiles.isEmpty() || !request.webHeaderDirectories.isEmpty()) {
                throw new IllegalArgumentException("WebIDL and web headers are only valid for a web component");
            }
            if(request.webModuleName != null && !request.webModuleName.trim().isEmpty()) {
                throw new IllegalArgumentException("webModuleName is only valid for a web component");
            }
        }

        HashSet<String> dependencyNames = new HashSet<>();
        for(NativeArchiveInput dependency : request.dependencyArchives) {
            if(!dependencyNames.add(dependency.getName())) {
                throw new IllegalArgumentException("Duplicate dependency archive name: " + dependency.getName());
            }
            NativeArchiveInspector.requireStaticArchive(dependency.getPath(),
                    "Dependency archive " + dependency.getName());
        }
        for(Path idl : request.webIDLFiles) {
            NativeArchiveInspector.requireRegularFile(idl, "WebIDL file");
        }
        for(Path directory : request.webHeaderDirectories) {
            if(directory == null || !Files.isDirectory(directory)) {
                throw new IllegalArgumentException("Web header directory does not exist: " + directory);
            }
        }
        if(request.licenseFiles.isEmpty()) {
            throw new IllegalArgumentException("Native component requires at least one license or notice file");
        }
        for(Path license : request.licenseFiles) {
            NativeArchiveInspector.requireRegularFile(license, "License file");
        }
        if(!request.dynamicDependencies.isEmpty()) {
            throw new IllegalArgumentException("Native components cannot declare non-system dynamic dependencies; "
                    + "bundle every dependency as a static archive or declare an operating-system library");
        }
    }

    private static void validateTargetBridge(NativeTarget target, NativeBridge bridge) {
        NativeTarget.OperatingSystem os = target.getOperatingSystem();
        if(os == NativeTarget.OperatingSystem.WEB && bridge != NativeBridge.WEB) {
            throw new IllegalArgumentException("Web target requires WEB bridge");
        }
        if(os != NativeTarget.OperatingSystem.WEB && bridge == NativeBridge.WEB) {
            throw new IllegalArgumentException("WEB bridge is only valid for web target");
        }
        if(os == NativeTarget.OperatingSystem.ANDROID && bridge != NativeBridge.JNI) {
            throw new IllegalArgumentException("Android fat components support JNI bridge only");
        }
        if(os == NativeTarget.OperatingSystem.IOS && bridge != NativeBridge.TEAVM_C) {
            throw new IllegalArgumentException("iOS fat components support TeaVM C bridge only");
        }
    }

    private static String normalizedPlatformVersion(NativeComponentRequest request) {
        String value = optional(request.minimumPlatformVersion);
        if(value.isEmpty()) {
            return "";
        }
        switch(request.target.getOperatingSystem()) {
            case ANDROID:
                try {
                    int api = Integer.parseInt(value);
                    if(api < 21 || api > 999) {
                        throw new IllegalArgumentException(
                                "Android minimum platform version must be an API level between 21 and 999: "
                                        + value);
                    }
                }
                catch(NumberFormatException exception) {
                    throw new IllegalArgumentException(
                            "Android minimum platform version must be a numeric API level: " + value,
                            exception);
                }
                break;
            case MACOS:
            case IOS:
                if(!value.matches("[0-9]+(?:\\.[0-9]+){0,3}")) {
                    throw new IllegalArgumentException(
                            request.target.getOperatingSystem().getId()
                                    + " minimum platform version must be numeric and dotted: " + value);
                }
                break;
            default:
                if(value.indexOf('\0') >= 0 || value.indexOf('\n') >= 0 || value.indexOf('\r') >= 0) {
                    throw new IllegalArgumentException(
                            "minimumPlatformVersion contains a control character");
                }
                break;
        }
        return value;
    }

    private static ArrayList<SourceEntry> collectSources(NativeComponentRequest request) throws IOException {
        ArrayList<SourceEntry> result = new ArrayList<>();
        result.add(new SourceEntry(
                NativeComponentFileRole.IMPLEMENTATION,
                "implementation",
                request.implementationArchive,
                "native/implementation/" + safeFileName(request.implementationArchive),
                NativeArchiveLinkMode.WHOLE_ARCHIVE));
        if(request.bridgeArchive != null) {
            result.add(new SourceEntry(
                    NativeComponentFileRole.BRIDGE,
                    "bridge",
                    request.bridgeArchive,
                    "native/bridge/" + safeFileName(request.bridgeArchive),
                    NativeArchiveLinkMode.WHOLE_ARCHIVE));
        }
        for(int i = 0; i < request.dependencyArchives.size(); i++) {
            NativeArchiveInput dependency = request.dependencyArchives.get(i);
            result.add(new SourceEntry(
                    NativeComponentFileRole.DEPENDENCY,
                    dependency.getName(),
                    dependency.getPath(),
                    "native/dependencies/" + String.format(Locale.ROOT, "%04d-", i)
                            + safeFileName(dependency.getPath()),
                    dependency.getLinkMode()));
        }
        for(int i = 0; i < request.webIDLFiles.size(); i++) {
            Path idl = request.webIDLFiles.get(i);
            result.add(new SourceEntry(
                    NativeComponentFileRole.WEB_IDL,
                    safeFileName(idl),
                    idl,
                    "web/idl/" + String.format(Locale.ROOT, "%04d-", i) + safeFileName(idl),
                    null));
        }
        int webHeaderCount = 0;
        for(int rootIndex = 0; rootIndex < request.webHeaderDirectories.size(); rootIndex++) {
            Path root = request.webHeaderDirectories.get(rootIndex).toAbsolutePath().normalize();
            List<Path> paths;
            try(java.util.stream.Stream<Path> stream = Files.walk(root)) {
                paths = stream.sorted(Comparator.comparing(Path::toString)).toList();
            }
            for(Path header : paths) {
                if(Files.isSymbolicLink(header)) {
                    throw new IllegalArgumentException("Web header tree cannot contain symbolic links: " + header);
                }
                if(!Files.isRegularFile(header)) {
                    continue;
                }
                if(!isWebHeader(header)) {
                    continue;
                }
                String relative = root.relativize(header.toAbsolutePath().normalize()).toString().replace('\\', '/');
                String entryPath = "web/include/" + rootIndex + "/" + relative;
                NativeComponentIO.requireSafeEntryPath(entryPath);
                result.add(new SourceEntry(
                        NativeComponentFileRole.WEB_HEADER,
                        Integer.toString(rootIndex),
                        header,
                        entryPath,
                        null));
                webHeaderCount++;
            }
        }
        if(request.target.getOperatingSystem() == NativeTarget.OperatingSystem.WEB
                && webHeaderCount == 0) {
            throw new IllegalArgumentException("Web component header directories contain no header files");
        }
        for(int i = 0; i < request.licenseFiles.size(); i++) {
            Path license = request.licenseFiles.get(i);
            result.add(new SourceEntry(
                    NativeComponentFileRole.LICENSE,
                    safeFileName(license),
                    license,
                    "META-INF/licenses/" + String.format(Locale.ROOT, "%04d-", i) + safeFileName(license),
                    null));
        }

        HashSet<String> paths = new HashSet<>();
        for(SourceEntry source : result) {
            if(!paths.add(source.entryPath)) {
                throw new IllegalArgumentException("Duplicate resource JAR entry: " + source.entryPath);
            }
        }
        return result;
    }

    private static boolean isWebHeader(Path path) {
        String fileName = path.getFileName().toString().toLowerCase(Locale.ROOT);
        return fileName.endsWith(".h")
                || fileName.endsWith(".hh")
                || fileName.endsWith(".hpp")
                || fileName.endsWith(".hxx")
                || fileName.endsWith(".inc")
                || fileName.endsWith(".inl");
    }

    private static void writeJar(Path output, NativeComponentManifest manifest, List<SourceEntry> sources)
            throws IOException {
        ArrayList<JarSource> jarSources = new ArrayList<>();
        jarSources.add(new JarSource(NativeComponentManifest.PATH, NativeComponentIO.writeManifest(manifest), null));
        for(SourceEntry source : sources) {
            jarSources.add(new JarSource(source.entryPath, null, source.sourcePath));
        }
        jarSources.sort(Comparator.comparing(source -> source.entryPath));

        try(OutputStream fileOutput = Files.newOutputStream(output);
            ZipOutputStream zip = new ZipOutputStream(fileOutput)) {
            zip.setLevel(9);
            for(JarSource source : jarSources) {
                ZipEntry entry = new ZipEntry(source.entryPath);
                entry.setLastModifiedTime(REPRODUCIBLE_TIMESTAMP);
                entry.setLastAccessTime(REPRODUCIBLE_TIMESTAMP);
                entry.setCreationTime(REPRODUCIBLE_TIMESTAMP);
                zip.putNextEntry(entry);
                if(source.bytes != null) {
                    zip.write(source.bytes);
                }
                else {
                    Files.copy(source.path, zip);
                }
                zip.closeEntry();
            }
        }
    }

    private static void moveReplacing(Path source, Path destination) throws IOException {
        try {
            Files.move(source, destination, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        }
        catch(AtomicMoveNotSupportedException ignored) {
            Files.move(source, destination, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static List<String> normalizedUniqueList(List<String> values, String description, boolean identifier) {
        ArrayList<String> result = new ArrayList<>();
        Set<String> unique = new HashSet<>();
        for(String value : values) {
            requireValue(description, value);
            String normalized = value.trim();
            if(normalized.indexOf('\0') >= 0 || normalized.indexOf('\n') >= 0 || normalized.indexOf('\r') >= 0) {
                throw new IllegalArgumentException(description + " contains a control character");
            }
            if(identifier && (normalized.contains("/") || normalized.contains("\\"))) {
                throw new IllegalArgumentException(description + " must be a name, not a path: " + value);
            }
            if(!unique.add(normalized)) {
                throw new IllegalArgumentException("Duplicate " + description + ": " + normalized);
            }
            result.add(normalized);
        }
        return Collections.unmodifiableList(result);
    }

    private static void requireIdentifier(String name, String value) {
        requireValue(name, value);
        if(!value.trim().matches("[A-Za-z0-9][A-Za-z0-9_.-]*")) {
            throw new IllegalArgumentException(name + " contains unsupported characters: " + value);
        }
    }

    private static void requireValue(String name, String value) {
        if(value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
    }

    private static String optional(String value) {
        return value == null ? "" : value.trim();
    }

    private static String safeFileName(Path path) {
        String name = path.getFileName().toString().replaceAll("[^A-Za-z0-9_.-]", "_");
        if(name.isEmpty() || name.equals(".") || name.equals("..")) {
            throw new IllegalArgumentException("Unsupported native component filename: " + path);
        }
        return name;
    }

    private static final class SourceEntry {
        private final NativeComponentFileRole role;
        private final String name;
        private final Path sourcePath;
        private final String entryPath;
        private final NativeArchiveLinkMode linkMode;

        private SourceEntry(NativeComponentFileRole role, String name, Path sourcePath, String entryPath,
                            NativeArchiveLinkMode linkMode) {
            this.role = role;
            this.name = name;
            this.sourcePath = sourcePath;
            this.entryPath = entryPath;
            this.linkMode = linkMode;
        }
    }

    private static final class JarSource {
        private final String entryPath;
        private final byte[] bytes;
        private final Path path;

        private JarSource(String entryPath, byte[] bytes, Path path) {
            this.entryPath = entryPath;
            this.bytes = bytes;
            this.path = path;
        }
    }
}
