package com.github.xpenatan.jParser.builder.bundle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class NativeBundleBuilderTest {
    @Rule
    public final TemporaryFolder temporaryFolder = new TemporaryFolder();

    private int fileIndex;

    @Test
    public void linksWindowsComponentsIntoOneRawLibrary() throws Exception {
        NativeTarget target = NativeTarget.of(
                NativeTarget.OperatingSystem.WINDOWS,
                NativeTarget.Architecture.X86_64);
        Path runtime = component("runtime", NativeComponentRole.RUNTIME, target, NativeBridge.JNI,
                "Java_runtime_init");
        Path graphics = component("graphics", NativeComponentRole.BINDING, target, NativeBridge.JNI,
                "Java_graphics_draw");
        RecordingExecutor executor = new RecordingExecutor();
        NativeBundleRequest request = bundleRequest("game", target, runtime, graphics);
        request.visualCppEnvironment = "vcvarsall.bat";

        NativeBundleResult result = NativeBundleBuilder.build(request, executor);

        assertEquals("game64.dll", result.getPrimaryOutputPath().getFileName().toString());
        assertEquals(1, executor.commands.size());
        List<String> command = executor.commands.get(0).getArguments();
        assertTrue(command.contains("/EXPORT:Java_runtime_init"));
        assertTrue(command.contains("/EXPORT:Java_graphics_draw"));
        assertEquals(4, command.stream().filter(value -> value.startsWith("/WHOLEARCHIVE:")).count());
        assertFalse(hasTemporaryBundleDirectory(request.outputDirectory));
    }

    @Test
    public void supportsMixedDesktopJniAndFfmWithFfmRuntime() throws Exception {
        NativeTarget target = NativeTarget.of(
                NativeTarget.OperatingSystem.LINUX,
                NativeTarget.Architecture.X86_64);
        Path runtime = component("runtime", NativeComponentRole.RUNTIME, target, NativeBridge.FFM,
                "runtime_init");
        Path graphics = component("graphics", NativeComponentRole.BINDING, target, NativeBridge.FFM,
                "graphics_draw");
        Path physics = component("physics", NativeComponentRole.BINDING, target, NativeBridge.JNI,
                "Java_physics_step");
        RecordingExecutor executor = new RecordingExecutor();
        NativeBundleRequest request = bundleRequest("game", target, runtime, graphics, physics);
        request.linkerExecutable = "test-g++";

        NativeBundleResult result = NativeBundleBuilder.build(request, executor);

        assertEquals("libgame64.so", result.getPrimaryOutputPath().getFileName().toString());
        List<String> command = executor.commands.get(0).getArguments();
        assertEquals("test-g++", command.get(0));
        assertTrue(command.contains("-Wl,--whole-archive"));
        assertTrue(command.contains("-Wl,--end-group"));

        Path wrongRuntime = component("runtime-jni", NativeComponentRole.RUNTIME, target, NativeBridge.JNI,
                "Java_runtime_jni_init");
        NativeBundleRequest invalid = bundleRequest("invalid", target, wrongRuntime, graphics, physics);
        IllegalArgumentException failure = assertThrows(
                IllegalArgumentException.class,
                () -> NativeBundleBuilder.build(invalid, executor));
        assertTrue(failure.getMessage().contains("ffm runtime"));
    }

    @Test
    public void rejectsDuplicateExportsAndIncompatibleToolchains() throws Exception {
        NativeTarget target = NativeTarget.of(
                NativeTarget.OperatingSystem.LINUX,
                NativeTarget.Architecture.X86_64);
        Path runtime = component("runtime", NativeComponentRole.RUNTIME, target, NativeBridge.JNI,
                "duplicate_symbol");
        Path binding = component("binding", NativeComponentRole.BINDING, target, NativeBridge.JNI,
                "duplicate_symbol");
        NativeBundleRequest duplicate = bundleRequest("game", target, runtime, binding);

        IllegalArgumentException duplicateFailure = assertThrows(
                IllegalArgumentException.class,
                () -> NativeBundleBuilder.build(duplicate, new RecordingExecutor()));
        assertTrue(duplicateFailure.getMessage().contains("Duplicate exported symbol"));

        Path otherToolchain = component(
                "other",
                NativeComponentRole.BINDING,
                target,
                NativeBridge.JNI,
                "other_symbol",
                "other-toolchain");
        NativeBundleRequest incompatible = bundleRequest("game", target, runtime, otherToolchain);
        IllegalArgumentException toolchainFailure = assertThrows(
                IllegalArgumentException.class,
                () -> NativeBundleBuilder.build(incompatible, new RecordingExecutor()));
        assertTrue(toolchainFailure.getMessage().contains("Incompatible toolchain"));
    }

    @Test
    public void requiresExactlyOneRuntimeAndMatchingRuntimeAbi() throws Exception {
        NativeTarget target = NativeTarget.of(
                NativeTarget.OperatingSystem.LINUX,
                NativeTarget.Architecture.X86_64);
        Path binding = component("binding", NativeComponentRole.BINDING, target, NativeBridge.JNI,
                "Java_binding_init");

        IllegalArgumentException missingRuntime = assertThrows(
                IllegalArgumentException.class,
                () -> NativeBundleBuilder.build(
                        bundleRequest("missing-runtime", target, binding),
                        new RecordingExecutor()));
        assertTrue(missingRuntime.getMessage().contains("exactly one jParser runtime component"));

        Path runtime = component("runtime", NativeComponentRole.RUNTIME, target, NativeBridge.JNI,
                "Java_runtime_init");
        Path secondRuntime = component("runtime-two", NativeComponentRole.RUNTIME, target, NativeBridge.JNI,
                "Java_runtime_two_init");
        IllegalArgumentException duplicateRuntime = assertThrows(
                IllegalArgumentException.class,
                () -> NativeBundleBuilder.build(
                        bundleRequest("duplicate-runtime", target, runtime, secondRuntime, binding),
                        new RecordingExecutor()));
        assertTrue(duplicateRuntime.getMessage().contains("found 2"));

        NativeComponentRequest incompatibleRequest = baseComponent(
                "incompatible-abi",
                NativeComponentRole.BINDING,
                target,
                NativeBridge.JNI,
                "test-toolchain");
        incompatibleRequest.runtimeAbi = "jparser-runtime-2";
        incompatibleRequest.bridgeArchive = staticArchive("incompatible-abi-bridge");
        incompatibleRequest.exportedSymbols.add("Java_incompatible_abi_init");
        Path incompatibleAbi = NativeComponentBuilder.build(incompatibleRequest).getPath();
        IllegalArgumentException abiFailure = assertThrows(
                IllegalArgumentException.class,
                () -> NativeBundleBuilder.build(
                        bundleRequest("incompatible-abi", target, runtime, incompatibleAbi),
                        new RecordingExecutor()));
        assertTrue(abiFailure.getMessage().contains("Incompatible runtime ABI"));
    }

    @Test
    public void treatsUnspecifiedOptionalToolchainMetadataAsCompatible() throws Exception {
        NativeTarget target = NativeTarget.of(
                NativeTarget.OperatingSystem.LINUX,
                NativeTarget.Architecture.X86_64);
        NativeComponentRequest runtimeRequest = baseComponent(
                "runtime",
                NativeComponentRole.RUNTIME,
                target,
                NativeBridge.JNI,
                "test-toolchain");
        runtimeRequest.toolchainVersion = "";
        runtimeRequest.bridgeArchive = staticArchive("runtime-bridge");
        runtimeRequest.exportedSymbols.add("Java_runtime_init");
        Path runtime = NativeComponentBuilder.build(runtimeRequest).getPath();
        Path versionOne = component(
                "version-one",
                NativeComponentRole.BINDING,
                target,
                NativeBridge.JNI,
                "Java_version_one_init");

        NativeBundleBuilder.build(
                bundleRequest("compatible-version", target, runtime, versionOne),
                new RecordingExecutor());

        NativeComponentRequest versionTwoRequest = baseComponent(
                "version-two",
                NativeComponentRole.BINDING,
                target,
                NativeBridge.JNI,
                "test-toolchain");
        versionTwoRequest.toolchainVersion = "2";
        versionTwoRequest.bridgeArchive = staticArchive("version-two-bridge");
        versionTwoRequest.exportedSymbols.add("Java_version_two_init");
        Path versionTwo = NativeComponentBuilder.build(versionTwoRequest).getPath();
        IllegalArgumentException versionFailure = assertThrows(
                IllegalArgumentException.class,
                () -> NativeBundleBuilder.build(
                        bundleRequest("incompatible-version", target, runtime, versionOne, versionTwo),
                        new RecordingExecutor()));
        assertTrue(versionFailure.getMessage().contains("Incompatible toolchain version"));
    }

    @Test
    public void plansAndroidAndIosRawOutputs() throws Exception {
        NativeTarget androidTarget = NativeTarget.android(
                NativeTarget.Architecture.ARM64,
                "arm64-v8a");
        Path androidRuntime = component("android-runtime", NativeComponentRole.RUNTIME,
                androidTarget, NativeBridge.JNI, "Java_runtime_init");
        Path androidBinding = component("android-binding", NativeComponentRole.BINDING,
                androidTarget, NativeBridge.JNI, "Java_binding_init");
        Path ndk = temporaryFolder.newFolder("ndk").toPath();
        Path toolchain = ndk.resolve("toolchains/llvm/prebuilt/test-host");
        Files.createDirectories(toolchain.resolve("sysroot"));
        NativeBundleRequest android = bundleRequest("mobile", androidTarget, androidRuntime, androidBinding);
        android.androidNdkHome = ndk.toString();
        android.linkerExecutable = "test-clang++";
        RecordingExecutor androidExecutor = new RecordingExecutor();

        NativeBundleResult androidResult = NativeBundleBuilder.build(android, androidExecutor);

        assertEquals("libmobile.so", androidResult.getPrimaryOutputPath().getFileName().toString());
        assertTrue(androidExecutor.commands.get(0).getArguments()
                .contains("--target=aarch64-linux-android28"));

        NativeTarget iosTarget = NativeTarget.ios(
                NativeTarget.Architecture.ARM64,
                "device");
        Path iosRuntime = component("ios-runtime", NativeComponentRole.RUNTIME,
                iosTarget, NativeBridge.TEAVM_C, "runtime_init");
        Path iosBinding = component("ios-binding", NativeComponentRole.BINDING,
                iosTarget, NativeBridge.TEAVM_C, "binding_init");
        NativeBundleRequest ios = bundleRequest("mobile", iosTarget, iosRuntime, iosBinding);
        ios.linkerExecutable = "test-libtool";
        RecordingExecutor iosExecutor = new RecordingExecutor();

        NativeBundleResult iosResult = NativeBundleBuilder.build(ios, iosExecutor);

        assertEquals("libmobile.a", iosResult.getPrimaryOutputPath().getFileName().toString());
        List<String> iosCommand = iosExecutor.commands.get(0).getArguments();
        assertEquals("test-libtool", iosCommand.get(0));
        assertTrue(iosCommand.contains("-static"));
    }

    @Test
    public void rejectsBundleTargetsBelowAComponentPlatformMinimum() throws Exception {
        NativeTarget target = NativeTarget.android(
                NativeTarget.Architecture.ARM64,
                "arm64-v8a");
        NativeComponentRequest runtimeRequest = baseComponent(
                "runtime",
                NativeComponentRole.RUNTIME,
                target,
                NativeBridge.JNI,
                "test-toolchain");
        runtimeRequest.minimumPlatformVersion = "29";
        runtimeRequest.bridgeArchive = staticArchive("runtime-bridge");
        runtimeRequest.exportedSymbols.add("Java_runtime_init");
        Path runtime = NativeComponentBuilder.build(runtimeRequest).getPath();
        Path binding = component(
                "binding",
                NativeComponentRole.BINDING,
                target,
                NativeBridge.JNI,
                "Java_binding_init");
        NativeBundleRequest request = bundleRequest("mobile", target, runtime, binding);
        request.androidApiLevel = 28;

        IllegalArgumentException failure = assertThrows(
                IllegalArgumentException.class,
                () -> NativeBundleBuilder.build(request, new RecordingExecutor()));

        assertTrue(failure.getMessage().contains("minimum Android API"));
        assertTrue(failure.getMessage().contains("29"));
    }

    @Test
    public void mergesWebIdlAndAliasesIntoOneJsWasmModule() throws Exception {
        NativeTarget target = NativeTarget.web(NativeTarget.Architecture.WASM32);
        Path runtime = webComponent(
                "runtime",
                NativeComponentRole.RUNTIME,
                target,
                "runtime",
                "interface RuntimeNative {};",
                "runtime_init");
        Path graphics = webComponent(
                "graphics",
                NativeComponentRole.BINDING,
                target,
                "jWebGPU",
                "interface GraphicsNative {};",
                "graphics_init");
        Path emscripten = temporaryFolder.newFolder("emscripten").toPath();
        Files.createDirectories(emscripten.resolve("tools"));
        Files.writeString(emscripten.resolve("tools/webidl_binder.py"), "# test");
        Files.writeString(emscripten.resolve("em++.bat"), "@echo off");

        NativeBundleRequest request = bundleRequest("game", target, runtime, graphics);
        request.emscriptenRoot = emscripten.toString();
        request.pythonExecutable = "test-python";
        request.linkerExecutable = "test-em++";
        request.keepTemporaryFiles = true;
        RecordingExecutor executor = new RecordingExecutor();

        NativeBundleResult result = NativeBundleBuilder.build(request, executor);

        assertEquals(List.of("game.js", "game.wasm"), result.getOutputPaths().stream()
                .map(path -> path.getFileName().toString())
                .toList());
        assertEquals(2, executor.commands.size());
        Path work = findTemporaryBundleDirectory(request.outputDirectory);
        String merged = Files.readString(work.resolve("web/IDLMerged.idl"));
        String aliases = Files.readString(work.resolve("web/jparser-bundle-aliases.js"));
        assertTrue(merged.contains("interface RuntimeNative"));
        assertTrue(merged.contains("interface GraphicsNative"));
        assertTrue(aliases.contains("scope[\"runtime\"] = Module"));
        assertTrue(aliases.contains("scope[\"jWebGPU\"] = Module"));
    }

    @Test
    public void rejectsDuplicateWebIdlDeclarationsBeforeRunningTools() throws Exception {
        NativeTarget target = NativeTarget.web(NativeTarget.Architecture.WASM32);
        Path runtime = webComponent(
                "runtime",
                NativeComponentRole.RUNTIME,
                target,
                "runtime",
                "interface SharedName {};",
                "runtime_init");
        Path binding = webComponent(
                "binding",
                NativeComponentRole.BINDING,
                target,
                "binding",
                "interface SharedName {};",
                "binding_init");
        Path emscripten = temporaryFolder.newFolder("duplicate-emscripten").toPath();
        Files.createDirectories(emscripten.resolve("tools"));
        Files.writeString(emscripten.resolve("tools/webidl_binder.py"), "# test");
        NativeBundleRequest request = bundleRequest("game", target, runtime, binding);
        request.emscriptenRoot = emscripten.toString();
        request.linkerExecutable = "test-em++";
        RecordingExecutor executor = new RecordingExecutor();

        IllegalArgumentException failure = assertThrows(
                IllegalArgumentException.class,
                () -> NativeBundleBuilder.build(request, executor));

        assertTrue(failure.getMessage().contains("Duplicate WebIDL declaration SharedName"));
        assertTrue(executor.commands.isEmpty());
    }

    private NativeBundleRequest bundleRequest(String name, NativeTarget target, Path... components)
            throws IOException {
        NativeBundleRequest request = new NativeBundleRequest();
        request.bundleName = name;
        request.target = target;
        request.outputDirectory = temporaryFolder.newFolder("output-" + fileIndex++).toPath();
        request.componentJars.addAll(List.of(components));
        return request;
    }

    private Path component(
            String componentId,
            NativeComponentRole role,
            NativeTarget target,
            NativeBridge bridge,
            String exportedSymbol) throws Exception {
        return component(componentId, role, target, bridge, exportedSymbol, "test-toolchain");
    }

    private Path component(
            String componentId,
            NativeComponentRole role,
            NativeTarget target,
            NativeBridge bridge,
            String exportedSymbol,
            String toolchain) throws Exception {
        NativeComponentRequest request = baseComponent(componentId, role, target, bridge, toolchain);
        request.bridgeArchive = staticArchive(componentId + "-bridge");
        request.exportedSymbols.add(exportedSymbol);
        return NativeComponentBuilder.build(request).getPath();
    }

    private Path webComponent(
            String componentId,
            NativeComponentRole role,
            NativeTarget target,
            String moduleName,
            String idlText,
            String exportedSymbol) throws Exception {
        NativeComponentRequest request = baseComponent(
                componentId,
                role,
                target,
                NativeBridge.WEB,
                "test-toolchain");
        request.webModuleName = moduleName;
        Path idl = temporaryFolder.newFile(componentId + "-" + fileIndex++ + ".idl").toPath();
        Files.writeString(idl, idlText);
        request.webIDLFiles.add(idl);
        Path headers = temporaryFolder.newFolder(componentId + "-headers-" + fileIndex++).toPath();
        Files.writeString(headers.resolve(componentId + ".h"), "#pragma once");
        request.webHeaderDirectories.add(headers);
        request.exportedSymbols.add(exportedSymbol);
        return NativeComponentBuilder.build(request).getPath();
    }

    private NativeComponentRequest baseComponent(
            String componentId,
            NativeComponentRole role,
            NativeTarget target,
            NativeBridge bridge,
            String toolchain) throws Exception {
        NativeComponentRequest request = new NativeComponentRequest();
        request.outputJar = temporaryFolder.getRoot().toPath()
                .resolve(componentId + "-" + fileIndex++ + ".jar");
        request.componentId = componentId;
        request.componentVersion = "1.0.0";
        request.role = role;
        request.target = target;
        request.bridge = bridge;
        request.minimumJavaVersion = bridge == NativeBridge.FFM ? 25 : 8;
        request.runtimeAbi = "jparser-runtime-1";
        request.toolchainId = toolchain;
        request.toolchainVersion = "1";
        request.cRuntime = "test-crt";
        request.cppRuntime = "test-cpp";
        request.implementationArchive = staticArchive(componentId + "-implementation");
        Path license = temporaryFolder.newFile(componentId + "-LICENSE-" + fileIndex++).toPath();
        Files.writeString(license, "test license");
        request.licenseFiles.add(license);
        return request;
    }

    private Path staticArchive(String name) throws IOException {
        Path path = temporaryFolder.newFile(name + "-" + fileIndex++ + ".a").toPath();
        byte[] payload = {0x4c, 0x01, (byte)fileIndex, 7};
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

    private boolean hasTemporaryBundleDirectory(Path outputDirectory) throws IOException {
        try(java.util.stream.Stream<Path> stream = Files.list(outputDirectory)) {
            return stream.anyMatch(path -> path.getFileName().toString().startsWith(".jparser-bundle-"));
        }
    }

    private Path findTemporaryBundleDirectory(Path outputDirectory) throws IOException {
        try(java.util.stream.Stream<Path> stream = Files.list(outputDirectory)) {
            return stream.filter(path -> path.getFileName().toString().startsWith(".jparser-bundle-"))
                    .findFirst()
                    .orElseThrow();
        }
    }

    private static final class RecordingExecutor implements NativeBundleCommandExecutor {
        private final ArrayList<NativeBundleCommand> commands = new ArrayList<>();

        @Override
        public void execute(NativeBundleCommand command) throws IOException {
            commands.add(command);
            for(Path output : command.getExpectedOutputs()) {
                Files.createDirectories(output.getParent());
                Files.write(output, new byte[] {1, 2, 3});
            }
        }
    }
}
