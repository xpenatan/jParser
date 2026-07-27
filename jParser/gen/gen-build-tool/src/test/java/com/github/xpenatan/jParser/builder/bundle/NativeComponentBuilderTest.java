package com.github.xpenatan.jParser.builder.bundle;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class NativeComponentBuilderTest {
    @Rule
    public final TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void buildsDeterministicValidatedComponentAndExtractsIt() throws Exception {
        Path implementation = staticArchive("implementation.lib", new byte[] {0x4c, 0x01, 1, 2, 3});
        Path bridge = staticArchive("bridge.lib", new byte[] {0x64, (byte)0x86, 4, 5});
        Path dependency = staticArchive("dependency.lib", new byte[] {0x4c, 0x01, 6, 7});
        Path license = textFile("LICENSE.txt", "test license");
        NativeComponentRequest first = windowsRequest("first.jar", implementation, bridge, license);
        first.dependencyArchives.add(new NativeArchiveInput(
                "physics",
                dependency,
                NativeArchiveLinkMode.NORMAL));
        first.systemLibraries.add("user32");
        first.exportedSymbols.add("Java_example_Native_call");
        first.dynamicDependencies.add("third-party.dll");

        IllegalArgumentException dynamicFailure = assertThrows(
                IllegalArgumentException.class,
                () -> NativeComponentBuilder.build(first));
        assertTrue(dynamicFailure.getMessage().contains("dynamic dependencies"));
        first.dynamicDependencies.clear();

        NativeComponentArtifact artifact = NativeComponentBuilder.build(first);
        NativeComponentManifest manifest = artifact.getManifest();

        assertEquals("windows-x86_64-jni-fast", artifact.getClassifier());
        assertEquals("sample", manifest.getComponentId());
        assertEquals("1.2.3", manifest.getComponentVersion());
        assertEquals(NativeBridge.JNI, manifest.getBridge());
        assertEquals(3, manifest.getFiles().stream()
                .filter(file -> file.getLinkMode() != null)
                .count());
        assertEquals("user32", manifest.getSystemLibraries().get(0));

        Path extracted = temporaryFolder.newFolder("extracted").toPath();
        NativeComponentReader.extract(artifact.getPath(), extracted);
        for(NativeComponentManifest.FileEntry file : manifest.getFiles()) {
            assertTrue(Files.isRegularFile(extracted.resolve(file.getPath())));
        }

        NativeComponentRequest second = windowsRequest("second.jar", implementation, bridge, license);
        second.dependencyArchives.add(new NativeArchiveInput(
                "physics",
                dependency,
                NativeArchiveLinkMode.NORMAL));
        second.systemLibraries.add("user32");
        second.exportedSymbols.add("Java_example_Native_call");
        NativeComponentBuilder.build(second);

        assertArrayEquals(Files.readAllBytes(first.outputJar), Files.readAllBytes(second.outputJar));
    }

    @Test
    public void rejectsImportLibrariesAndThinArchives() throws Exception {
        Path importLibrary = staticArchive("import.lib", new byte[] {0, 0, (byte)0xff, (byte)0xff, 1});
        Path bridge = staticArchive("bridge.lib", new byte[] {0x4c, 0x01, 1});
        Path license = textFile("LICENSE.txt", "test license");
        NativeComponentRequest request = windowsRequest("component.jar", importLibrary, bridge, license);

        IllegalArgumentException importFailure = assertThrows(
                IllegalArgumentException.class,
                () -> NativeComponentBuilder.build(request));
        assertTrue(importFailure.getMessage().contains("import library"));

        Path thin = temporaryFolder.newFile("thin.a").toPath();
        Files.write(thin, "!<thin>\n".getBytes(StandardCharsets.US_ASCII));
        request.implementationArchive = thin;
        IllegalArgumentException thinFailure = assertThrows(
                IllegalArgumentException.class,
                () -> NativeComponentBuilder.build(request));
        assertTrue(thinFailure.getMessage().contains("thin archive"));
    }

    @Test
    public void rejectsFfmBelowJava25() throws Exception {
        Path implementation = staticArchive("implementation.lib", new byte[] {0x4c, 0x01, 1});
        Path bridge = staticArchive("bridge.lib", new byte[] {0x4c, 0x01, 2});
        Path license = textFile("LICENSE.txt", "test license");
        NativeComponentRequest request = windowsRequest("component.jar", implementation, bridge, license);
        request.bridge = NativeBridge.FFM;
        request.minimumJavaVersion = 21;

        IllegalArgumentException failure = assertThrows(
                IllegalArgumentException.class,
                () -> NativeComponentBuilder.build(request));

        assertTrue(failure.getMessage().contains("Java version 25"));
    }

    @Test
    public void preservesCanonicalAndroidAbiInClassifierAndRecordsPlatformMinimum() throws Exception {
        Path implementation = staticArchive("implementation.a", new byte[] {0x7f, 0x45, 1});
        Path bridge = staticArchive("bridge.a", new byte[] {0x7f, 0x45, 2});
        Path license = textFile("LICENSE.txt", "test license");
        NativeComponentRequest request = baseRequest("android.jar", implementation, license);
        request.target = NativeTarget.android(NativeTarget.Architecture.X86_64, "x86_64");
        request.bridge = NativeBridge.JNI;
        request.bridgeArchive = bridge;
        request.minimumPlatformVersion = "29";

        NativeComponentManifest manifest = NativeComponentBuilder.build(request).getManifest();

        assertEquals("android-x86_64-jni-fast", manifest.getClassifier());
        assertEquals("x86_64", manifest.getTarget().getAbi());
        assertEquals("29", manifest.getMinimumPlatformVersion());
    }

    @Test
    public void webComponentCarriesIdlAndHeaderTreeWithoutBridgeArchive() throws Exception {
        Path implementation = staticArchive("implementation.a", new byte[] {0x7f, 0x45, 1});
        Path license = textFile("LICENSE.txt", "test license");
        Path idl = textFile("sample.idl", "interface Sample {};");
        Path headers = temporaryFolder.newFolder("headers").toPath();
        Files.createDirectories(headers.resolve("sample"));
        Files.writeString(headers.resolve("sample/sample.h"), "#pragma once");

        NativeComponentRequest request = baseRequest("web.jar", implementation, license);
        request.target = NativeTarget.web(NativeTarget.Architecture.WASM32);
        request.bridge = NativeBridge.WEB;
        request.webModuleName = "sample";
        request.webIDLFiles.add(idl);
        request.webHeaderDirectories.add(headers);

        NativeComponentManifest manifest = NativeComponentBuilder.build(request).getManifest();

        assertEquals("web-fast", manifest.getClassifier());
        assertTrue(manifest.getFiles().stream().anyMatch(file -> file.getRole().equals("WEB_IDL")));
        assertTrue(manifest.getFiles().stream().anyMatch(file ->
                file.getPath().equals("web/include/0/sample/sample.h")));
        assertTrue(manifest.getFiles().stream().noneMatch(file -> file.getRole().equals("BRIDGE")));
    }

    @Test
    public void detectsPayloadTamperingAndTraversalManifest() throws Exception {
        Path implementation = staticArchive("implementation.lib", new byte[] {0x4c, 0x01, 1});
        Path bridge = staticArchive("bridge.lib", new byte[] {0x4c, 0x01, 2});
        Path license = textFile("LICENSE.txt", "test license");
        NativeComponentRequest request = windowsRequest("component.jar", implementation, bridge, license);
        Path original = NativeComponentBuilder.build(request).getPath();

        Path tampered = temporaryFolder.newFile("tampered.jar").toPath();
        rewriteJar(original, tampered, (name, bytes) -> {
            if(name.startsWith("native/implementation/")) {
                bytes[bytes.length - 1] ^= 1;
            }
            return bytes;
        });
        IllegalArgumentException checksumFailure = assertThrows(
                IllegalArgumentException.class,
                () -> NativeComponentReader.read(tampered));
        assertTrue(checksumFailure.getMessage().contains("checksum mismatch"));

        Path traversal = temporaryFolder.newFile("traversal.jar").toPath();
        rewriteJar(original, traversal, (name, bytes) -> {
            if(name.equals(NativeComponentManifest.PATH)) {
                String manifest = new String(bytes, StandardCharsets.UTF_8);
                manifest = manifest.replaceFirst(
                        "file\\.0\\.path=native/implementation/[^\\n]+",
                        "file.0.path=../outside.lib");
                return manifest.getBytes(StandardCharsets.UTF_8);
            }
            return bytes;
        });
        IllegalArgumentException traversalFailure = assertThrows(
                IllegalArgumentException.class,
                () -> NativeComponentReader.read(traversal));
        assertTrue(traversalFailure.getMessage().contains("Unsafe native component entry path"));
    }

    private NativeComponentRequest windowsRequest(
            String outputName,
            Path implementation,
            Path bridge,
            Path license) {
        NativeComponentRequest request = baseRequest(outputName, implementation, license);
        request.target = NativeTarget.of(
                NativeTarget.OperatingSystem.WINDOWS,
                NativeTarget.Architecture.X86_64);
        request.bridge = NativeBridge.JNI;
        request.bridgeArchive = bridge;
        return request;
    }

    private NativeComponentRequest baseRequest(String outputName, Path implementation, Path license) {
        NativeComponentRequest request = new NativeComponentRequest();
        request.outputJar = temporaryFolder.getRoot().toPath().resolve(outputName);
        request.componentId = "sample";
        request.componentVersion = "1.2.3";
        request.variantName = "fast";
        request.runtimeAbi = "1";
        request.toolchainId = "test-toolchain";
        request.implementationArchive = implementation;
        request.licenseFiles.add(license);
        return request;
    }

    private Path staticArchive(String name, byte[] payload) throws IOException {
        Path path = temporaryFolder.newFile(name).toPath();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        output.write("!<arch>\n".getBytes(StandardCharsets.US_ASCII));
        output.write(field("object.o/", 16));
        output.write(field("0", 12));
        output.write(field("0", 6));
        output.write(field("0", 6));
        output.write(field("100644", 8));
        output.write(field(Integer.toString(payload.length), 10));
        output.write('`');
        output.write('\n');
        output.write(payload);
        if((payload.length & 1) != 0) {
            output.write('\n');
        }
        Files.write(path, output.toByteArray());
        return path;
    }

    private byte[] field(String value, int width) {
        StringBuilder result = new StringBuilder(value);
        while(result.length() < width) {
            result.append(' ');
        }
        return result.toString().getBytes(StandardCharsets.US_ASCII);
    }

    private Path textFile(String name, String value) throws IOException {
        Path path = temporaryFolder.newFile(name).toPath();
        Files.writeString(path, value);
        return path;
    }

    private void rewriteJar(Path input, Path output, EntryTransform transform) throws IOException {
        Map<String, byte[]> entries = new LinkedHashMap<>();
        try(ZipFile zip = new ZipFile(input.toFile())) {
            Enumeration<? extends ZipEntry> enumeration = zip.entries();
            while(enumeration.hasMoreElements()) {
                ZipEntry entry = enumeration.nextElement();
                entries.put(entry.getName(), zip.getInputStream(entry).readAllBytes());
            }
        }
        try(ZipOutputStream zip = new ZipOutputStream(Files.newOutputStream(output))) {
            for(Map.Entry<String, byte[]> entry : entries.entrySet()) {
                zip.putNextEntry(new ZipEntry(entry.getKey()));
                zip.write(transform.transform(entry.getKey(), entry.getValue()));
                zip.closeEntry();
            }
        }
    }

    private interface EntryTransform {
        byte[] transform(String name, byte[] bytes);
    }
}
