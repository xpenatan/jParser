package com.github.xpenatan.jParser.builder.bundle;

import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class NativeResourcesPublicationVerifierTest {
    @Rule
    public final TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void verifiesCompleteClassifierMatrixAndComponentVersion() throws Exception {
        Path component = component("1.0.0");
        NativeResourcesPublicationRequest request = new NativeResourcesPublicationRequest();
        request.componentId = "sample";
        request.componentVersion = "1.0.0";
        request.resourceJars.add(component);
        request.declaredClassifiers.add("windows-x86_64-jni");
        request.requireCompleteMatrix = true;

        NativeResourcesPublicationVerifier.verify(request);

        request.declaredClassifiers.clear();
        request.declaredClassifiers.add("linux-x86_64-jni");
        IllegalArgumentException matrixFailure = assertThrows(
                IllegalArgumentException.class,
                () -> NativeResourcesPublicationVerifier.verify(request));
        assertTrue(matrixFailure.getMessage().contains("Missing classifiers"));

        request.declaredClassifiers.clear();
        request.declaredClassifiers.add("windows-x86_64-jni");
        request.componentVersion = "2.0.0";
        IllegalArgumentException versionFailure = assertThrows(
                IllegalArgumentException.class,
                () -> NativeResourcesPublicationVerifier.verify(request));
        assertTrue(versionFailure.getMessage().contains("version mismatch"));
    }

    private Path component(String version) throws Exception {
        NativeComponentRequest request = new NativeComponentRequest();
        request.outputJar = temporaryFolder.getRoot().toPath().resolve("component.jar");
        request.componentId = "sample";
        request.componentVersion = version;
        request.target = NativeTarget.of(
                NativeTarget.OperatingSystem.WINDOWS,
                NativeTarget.Architecture.X86_64);
        request.bridge = NativeBridge.JNI;
        request.runtimeAbi = "1";
        request.toolchainId = "test";
        request.implementationArchive = archive("implementation.lib", (byte)1);
        request.bridgeArchive = archive("bridge.lib", (byte)2);
        Path license = temporaryFolder.newFile("LICENSE").toPath();
        Files.writeString(license, "license");
        request.licenseFiles.add(license);
        return NativeComponentBuilder.build(request).getPath();
    }

    private Path archive(String name, byte value) throws Exception {
        Path path = temporaryFolder.newFile(name).toPath();
        byte[] payload = {0x4c, 0x01, value, 1};
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
}
