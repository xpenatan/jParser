package com.github.xpenatan.jParser.builder.bundle;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public final class NativeComponentReader {
    private static final int MAXIMUM_MANIFEST_BYTES = 5 * 1024 * 1024;

    private NativeComponentReader() {
    }

    public static NativeComponentManifest read(Path componentJar) throws IOException {
        return inspect(componentJar).manifest;
    }

    public static NativeComponentManifest extract(Path componentJar, Path destinationDirectory) throws IOException {
        Inspection inspection = inspect(componentJar);
        Path destination = destinationDirectory.toAbsolutePath().normalize();
        Files.createDirectories(destination);
        try(ZipFile zip = new ZipFile(componentJar.toFile())) {
            for(NativeComponentManifest.FileEntry file : inspection.manifest.getFiles()) {
                ZipEntry entry = zip.getEntry(file.getPath());
                Path output = destination.resolve(file.getPath()).normalize();
                if(!output.startsWith(destination)) {
                    throw new IllegalArgumentException("Native component entry escapes extraction directory: "
                            + file.getPath());
                }
                Files.createDirectories(output.getParent());
                try(InputStream input = zip.getInputStream(entry)) {
                    Files.copy(input, output, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
        return inspection.manifest;
    }

    private static Inspection inspect(Path componentJar) throws IOException {
        NativeArchiveInspector.requireRegularFile(componentJar, "Native component JAR");
        try(ZipFile zip = new ZipFile(componentJar.toFile())) {
            Map<String, ZipEntry> entries = entries(zip);
            ZipEntry manifestEntry = entries.get(NativeComponentManifest.PATH);
            if(manifestEntry == null) {
                throw new IllegalArgumentException("Native component is missing "
                        + NativeComponentManifest.PATH + ": " + componentJar);
            }
            NativeComponentManifest manifest;
            try(InputStream input = zip.getInputStream(manifestEntry)) {
                byte[] manifestBytes = NativeComponentIO.readLimited(input, MAXIMUM_MANIFEST_BYTES,
                        "Native component manifest");
                manifest = NativeComponentIO.readManifest(new java.io.ByteArrayInputStream(manifestBytes));
            }

            Set<String> expectedPaths = new HashSet<>();
            expectedPaths.add(NativeComponentManifest.PATH);
            for(NativeComponentManifest.FileEntry file : manifest.getFiles()) {
                expectedPaths.add(file.getPath());
                ZipEntry entry = entries.get(file.getPath());
                if(entry == null || entry.isDirectory()) {
                    throw new IllegalArgumentException("Native component is missing payload: " + file.getPath());
                }
                String actualHash;
                try(InputStream input = zip.getInputStream(entry)) {
                    actualHash = NativeComponentIO.sha256(input);
                }
                if(!actualHash.equals(file.getSha256())) {
                    throw new IllegalArgumentException("Native component checksum mismatch for " + file.getPath());
                }
            }
            for(String entryPath : entries.keySet()) {
                if(!expectedPaths.contains(entryPath)) {
                    throw new IllegalArgumentException("Native component contains an unlisted entry: " + entryPath);
                }
            }
            return new Inspection(manifest);
        }
    }

    private static Map<String, ZipEntry> entries(ZipFile zip) {
        HashMap<String, ZipEntry> result = new HashMap<>();
        Enumeration<? extends ZipEntry> enumeration = zip.entries();
        while(enumeration.hasMoreElements()) {
            ZipEntry entry = enumeration.nextElement();
            if(entry.isDirectory()) {
                continue;
            }
            String name = entry.getName();
            NativeComponentIO.requireSafeEntryPath(name);
            if(result.put(name, entry) != null) {
                throw new IllegalArgumentException("Native component contains duplicate ZIP entry: " + name);
            }
        }
        return result;
    }

    private static final class Inspection {
        private final NativeComponentManifest manifest;

        private Inspection(NativeComponentManifest manifest) {
            this.manifest = manifest;
        }
    }
}
