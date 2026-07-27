package com.github.xpenatan.jParser.builder.bundle;

import com.github.xpenatan.jParser.builder.targets.WindowsMSVCTarget;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public final class NativeBundleBuilder {
    private static final Pattern BUNDLE_NAME = Pattern.compile("[A-Za-z0-9][A-Za-z0-9_-]*");
    private static final Pattern COMPONENT_ID = Pattern.compile("[A-Za-z0-9][A-Za-z0-9_.-]*");
    private static final Pattern NATIVE_NAME = Pattern.compile("[A-Za-z0-9][A-Za-z0-9_.+-]*");
    private static final Pattern EXPORTED_SYMBOL = Pattern.compile("[A-Za-z_?$@][A-Za-z0-9_?$@.]*");
    private static final Pattern WEB_DECLARATION = Pattern.compile(
            "(?m)^\\s*(?:\\[[^\\]\\r\\n]*\\]\\s*)?(partial\\s+)?"
                    + "(?:interface|enum|dictionary)\\s+([A-Za-z_$][A-Za-z0-9_$]*)\\b");

    private NativeBundleBuilder() {
    }

    public static NativeBundleResult build(NativeBundleRequest request) throws IOException {
        return build(request, ProcessNativeBundleCommandExecutor.INSTANCE);
    }

    static NativeBundleResult build(NativeBundleRequest request, NativeBundleCommandExecutor executor)
            throws IOException {
        if(executor == null) {
            throw new IllegalArgumentException("Native bundle command executor is required");
        }
        ValidatedRequest validated = validate(request);
        Files.createDirectories(validated.outputDirectory);
        Path workDirectory = Files.createTempDirectory(validated.outputDirectory, ".jparser-bundle-");
        try {
            ArrayList<ExtractedComponent> extracted = extract(validated.components, workDirectory);
            NativeBundlePlan plan = plan(validated, extracted, workDirectory);
            for(NativeBundleCommand command : plan.commands) {
                try {
                    executor.execute(command);
                }
                catch(InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    throw new IOException("Interrupted while building native bundle " + validated.bundleName,
                            exception);
                }
            }
            for(Path output : plan.outputs) {
                if(!Files.isRegularFile(output)) {
                    throw new IOException("Native bundle did not produce expected output: " + output);
                }
            }
            return new NativeBundleResult(plan.outputs);
        }
        finally {
            if(!request.keepTemporaryFiles) {
                deleteTemporaryDirectory(workDirectory, validated.outputDirectory);
            }
        }
    }

    private static ValidatedRequest validate(NativeBundleRequest request) throws IOException {
        if(request == null) {
            throw new IllegalArgumentException("Native bundle request is required");
        }
        String bundleName = required("bundleName", request.bundleName);
        if(!BUNDLE_NAME.matcher(bundleName).matches()) {
            throw new IllegalArgumentException("bundleName contains unsupported characters: " + bundleName);
        }
        if(request.outputDirectory == null) {
            throw new IllegalArgumentException("outputDirectory is required");
        }
        if(request.target == null) {
            throw new IllegalArgumentException("target is required");
        }
        if(request.buildType == null) {
            throw new IllegalArgumentException("buildType is required");
        }
        if(request.webOutput == null) {
            throw new IllegalArgumentException("webOutput is required");
        }
        if(request.componentJars.isEmpty()) {
            throw new IllegalArgumentException("At least one native component resource JAR is required");
        }
        if(request.androidApiLevel < 21 || request.androidApiLevel > 999) {
            throw new IllegalArgumentException("androidApiLevel must be between 21 and 999");
        }
        validateVersion("minimumMacOSVersion", request.minimumMacOSVersion);
        validateVersion("minimumIOSVersion", request.minimumIOSVersion);
        validateOptionalCommand("linkerExecutable", request.linkerExecutable);
        validateOptionalCommand("visualCppEnvironment", request.visualCppEnvironment);
        validateOptionalCommand("androidNdkHome", request.androidNdkHome);
        validateOptionalCommand("emscriptenRoot", request.emscriptenRoot);
        validateOptionalCommand("pythonExecutable", request.pythonExecutable);
        validateEnvironment(request.environment);

        Path outputDirectory = request.outputDirectory.toAbsolutePath().normalize();
        LinkedHashSet<Path> componentPaths = new LinkedHashSet<>();
        ArrayList<ComponentInput> components = new ArrayList<>();
        for(Path componentJar : request.componentJars) {
            NativeArchiveInspector.requireRegularFile(componentJar, "Native component resource JAR");
            Path normalized = componentJar.toAbsolutePath().normalize();
            if(!componentPaths.add(normalized)) {
                throw new IllegalArgumentException("Duplicate native component resource JAR: " + normalized);
            }
            NativeComponentManifest manifest = NativeComponentReader.read(normalized);
            components.add(new ComponentInput(normalized, manifest));
        }

        validateComponents(request, components);
        return new ValidatedRequest(request, bundleName, outputDirectory, components);
    }

    private static void validateComponents(NativeBundleRequest request, List<ComponentInput> components) {
        ComponentInput reference = components.get(0);
        NativeComponentManifest referenceManifest = reference.manifest;
        LinkedHashMap<String, Path> componentIds = new LinkedHashMap<>();
        LinkedHashMap<String, String> exports = new LinkedHashMap<>();
        LinkedHashMap<String, String> webModules = new LinkedHashMap<>();
        int runtimeCount = 0;
        NativeComponentManifest runtime = null;
        boolean hasFfm = false;
        boolean hasLicense;

        for(ComponentInput component : components) {
            NativeComponentManifest manifest = component.manifest;
            String componentId = manifest.getComponentId();
            if(!COMPONENT_ID.matcher(componentId).matches()) {
                throw new IllegalArgumentException("Invalid native component ID " + componentId
                        + " in " + component.jar);
            }
            Path previousComponent = componentIds.put(componentId, component.jar);
            if(previousComponent != null) {
                throw new IllegalArgumentException("Duplicate native component ID " + componentId
                        + " in " + previousComponent + " and " + component.jar);
            }
            required("component version", manifest.getComponentVersion());

            if(!manifest.getTarget().equals(request.target)) {
                throw incompatible(component, "target", request.target.toString(),
                        manifest.getTarget().toString());
            }
            if(manifest.getBuildType() != request.buildType) {
                throw incompatible(component, "build type", request.buildType.name(),
                        manifest.getBuildType().name());
            }
            validateMinimumPlatformVersion(request, component);
            requireEqual(reference, component, "runtime ABI",
                    referenceManifest.getRuntimeAbi(), manifest.getRuntimeAbi());
            requireEqual(reference, component, "toolchain",
                    referenceManifest.getToolchainId(), manifest.getToolchainId());

            if(manifest.getRole() == NativeComponentRole.RUNTIME) {
                runtimeCount++;
                runtime = manifest;
            }
            if(manifest.getBridge() == NativeBridge.FFM) {
                hasFfm = true;
                if(manifest.getMinimumJavaVersion() < 25) {
                    throw new IllegalArgumentException("FFM native component " + componentId
                            + " requires minimum Java version 25");
                }
            }
            if(manifest.getMinimumJavaVersion() < 8) {
                throw new IllegalArgumentException("Native component " + componentId
                        + " has an invalid minimum Java version: " + manifest.getMinimumJavaVersion());
            }

            hasLicense = false;
            for(NativeComponentManifest.FileEntry file : manifest.getFiles()) {
                if(file.role() == NativeComponentFileRole.LICENSE) {
                    hasLicense = true;
                }
            }
            if(!hasLicense) {
                throw new IllegalArgumentException("Native component " + componentId
                        + " does not contain a license");
            }

            for(String systemLibrary : manifest.getSystemLibraries()) {
                validateNativeName("system library", systemLibrary, component);
            }
            for(String framework : manifest.getFrameworks()) {
                validateNativeName("framework", framework, component);
            }
            if(!manifest.getFrameworks().isEmpty()
                    && request.target.getOperatingSystem() != NativeTarget.OperatingSystem.MACOS
                    && request.target.getOperatingSystem() != NativeTarget.OperatingSystem.IOS) {
                throw new IllegalArgumentException("Framework dependencies are only valid for Apple targets: "
                        + component.jar);
            }
            for(String linkerOption : manifest.getLinkerOptions()) {
                validateLinkerOption(linkerOption, component);
            }
            for(String symbol : manifest.getExportedSymbols()) {
                if(!EXPORTED_SYMBOL.matcher(symbol).matches()) {
                    throw new IllegalArgumentException("Invalid exported symbol " + symbol + " in "
                            + component.jar);
                }
                String previous = exports.put(symbol, componentId);
                if(previous != null) {
                    throw new IllegalArgumentException("Duplicate exported symbol " + symbol
                            + " in native components " + previous + " and " + componentId);
                }
            }

            if(request.target.getOperatingSystem() == NativeTarget.OperatingSystem.WEB) {
                String moduleName = required("web module name", manifest.getWebModuleName());
                if(!COMPONENT_ID.matcher(moduleName).matches()) {
                    throw new IllegalArgumentException("Invalid web module name " + moduleName + " in "
                            + component.jar);
                }
                String previous = webModules.put(moduleName, componentId);
                if(previous != null) {
                    throw new IllegalArgumentException("Duplicate web module name " + moduleName
                            + " in native components " + previous + " and " + componentId);
                }
            }
            else if(!manifest.getWebModuleName().isEmpty()) {
                throw new IllegalArgumentException("Non-web native component contains a web module name: "
                        + component.jar);
            }
        }

        requireCompatibleOptional(
                components,
                "toolchain version",
                NativeComponentManifest::getToolchainVersion);
        requireCompatibleOptional(
                components,
                "C runtime",
                NativeComponentManifest::getCRuntime);
        requireCompatibleOptional(
                components,
                "C++ runtime",
                NativeComponentManifest::getCppRuntime);
        if(runtimeCount != 1) {
            throw new IllegalArgumentException("Native bundle requires exactly one jParser runtime component; found "
                    + runtimeCount);
        }
        validateBridgeCombination(request.target, components, runtime, hasFfm);
        validateConflictingOptions(components);
    }

    private static void validateMinimumPlatformVersion(
            NativeBundleRequest request,
            ComponentInput component) {
        String componentMinimum = component.manifest.getMinimumPlatformVersion();
        if(componentMinimum.isEmpty()) {
            return;
        }
        switch(request.target.getOperatingSystem()) {
            case ANDROID:
                int requiredApi;
                try {
                    requiredApi = Integer.parseInt(componentMinimum);
                }
                catch(NumberFormatException exception) {
                    throw incompatible(component, "minimum Android API",
                            Integer.toString(request.androidApiLevel), componentMinimum);
                }
                if(request.androidApiLevel < requiredApi) {
                    throw incompatible(component, "minimum Android API",
                            Integer.toString(request.androidApiLevel), componentMinimum);
                }
                break;
            case MACOS:
                if(compareVersions(request.minimumMacOSVersion, componentMinimum) < 0) {
                    throw incompatible(component, "minimum macOS version",
                            request.minimumMacOSVersion, componentMinimum);
                }
                break;
            case IOS:
                if(compareVersions(request.minimumIOSVersion, componentMinimum) < 0) {
                    throw incompatible(component, "minimum iOS version",
                            request.minimumIOSVersion, componentMinimum);
                }
                break;
            default:
                break;
        }
    }

    private static void validateBridgeCombination(
            NativeTarget target,
            List<ComponentInput> components,
            NativeComponentManifest runtime,
            boolean hasFfm) {
        NativeTarget.OperatingSystem operatingSystem = target.getOperatingSystem();
        NativeBridge requiredRuntimeBridge;
        switch(operatingSystem) {
            case WINDOWS:
            case LINUX:
            case MACOS:
                requiredRuntimeBridge = hasFfm ? NativeBridge.FFM : NativeBridge.JNI;
                for(ComponentInput component : components) {
                    NativeBridge bridge = component.manifest.getBridge();
                    if(bridge != NativeBridge.JNI && bridge != NativeBridge.FFM) {
                        throw new IllegalArgumentException("Desktop native bundle accepts only JNI and FFM "
                                + "components: " + component.jar);
                    }
                }
                break;
            case ANDROID:
                requiredRuntimeBridge = NativeBridge.JNI;
                requireOnlyBridge(components, NativeBridge.JNI, "Android");
                break;
            case IOS:
                requiredRuntimeBridge = NativeBridge.TEAVM_C;
                requireOnlyBridge(components, NativeBridge.TEAVM_C, "iOS");
                break;
            case WEB:
                requiredRuntimeBridge = NativeBridge.WEB;
                requireOnlyBridge(components, NativeBridge.WEB, "Web");
                break;
            default:
                throw new IllegalArgumentException("Unsupported native bundle target: " + operatingSystem);
        }
        if(runtime.getBridge() != requiredRuntimeBridge) {
            throw new IllegalArgumentException("Native bundle requires the " + requiredRuntimeBridge.getId()
                    + " runtime component for target " + target + " but found "
                    + runtime.getBridge().getId());
        }
    }

    private static void requireOnlyBridge(
            List<ComponentInput> components,
            NativeBridge expected,
            String targetName) {
        for(ComponentInput component : components) {
            if(component.manifest.getBridge() != expected) {
                throw new IllegalArgumentException(targetName + " native bundle requires "
                        + expected.getId() + " components: " + component.jar);
            }
        }
    }

    private static void validateConflictingOptions(List<ComponentInput> components) {
        HashSet<String> options = new HashSet<>();
        for(ComponentInput component : components) {
            options.addAll(component.manifest.getLinkerOptions());
        }
        rejectPair(options, "-static-libstdc++", "-shared-libstdc++");
        rejectPair(options, "-static-libgcc", "-shared-libgcc");
    }

    private static void rejectPair(Set<String> options, String first, String second) {
        if(options.contains(first) && options.contains(second)) {
            throw new IllegalArgumentException("Native components contain conflicting linker options: "
                    + first + " and " + second);
        }
    }

    private static ArrayList<ExtractedComponent> extract(List<ComponentInput> components, Path workDirectory)
            throws IOException {
        ArrayList<ExtractedComponent> result = new ArrayList<>();
        for(int i = 0; i < components.size(); i++) {
            ComponentInput component = components.get(i);
            Path root = workDirectory.resolve(String.format(Locale.ROOT, "component-%04d", i));
            NativeComponentReader.extract(component.jar, root);
            ArrayList<ArchiveFile> archives = new ArrayList<>();
            ArrayList<Path> idlFiles = new ArrayList<>();
            LinkedHashSet<Path> headerRoots = new LinkedHashSet<>();
            for(NativeComponentManifest.FileEntry file : component.manifest.getFiles()) {
                Path extracted = root.resolve(file.getPath()).normalize();
                switch(file.role()) {
                    case IMPLEMENTATION:
                    case BRIDGE:
                    case DEPENDENCY:
                        NativeArchiveInspector.requireStaticArchive(extracted,
                                component.manifest.getComponentId() + " " + file.getRole().toLowerCase(Locale.ROOT)
                                        + " archive");
                        archives.add(new ArchiveFile(extracted, file.getLinkMode()));
                        break;
                    case WEB_IDL:
                        idlFiles.add(extracted);
                        break;
                    case WEB_HEADER:
                        headerRoots.add(root.resolve("web/include").resolve(file.getName()).normalize());
                        break;
                    case LICENSE:
                        break;
                    default:
                        throw new IllegalArgumentException("Unsupported native component file role: "
                                + file.getRole());
                }
            }
            result.add(new ExtractedComponent(component, root, archives, idlFiles,
                    new ArrayList<>(headerRoots)));
        }
        return result;
    }

    private static NativeBundlePlan plan(
            ValidatedRequest request,
            List<ExtractedComponent> components,
            Path workDirectory) throws IOException {
        switch(request.request.target.getOperatingSystem()) {
            case WINDOWS:
                return planWindows(request, components, workDirectory);
            case LINUX:
                return planLinux(request, components, workDirectory);
            case MACOS:
                return planMacOS(request, components, workDirectory);
            case ANDROID:
                return planAndroid(request, components, workDirectory);
            case IOS:
                return planIOS(request, components, workDirectory);
            case WEB:
                return planWeb(request, components, workDirectory);
            default:
                throw new IllegalArgumentException("Unsupported native bundle target: " + request.request.target);
        }
    }

    private static NativeBundlePlan planWindows(
            ValidatedRequest request,
            List<ExtractedComponent> components,
            Path workDirectory) {
        String architecture;
        switch(request.request.target.getArchitecture()) {
            case X86:
                architecture = "x86";
                break;
            case X86_64:
                architecture = "x64";
                break;
            case ARM64:
                architecture = "arm64";
                break;
            default:
                throw new IllegalArgumentException("Unsupported Windows architecture: "
                        + request.request.target.getArchitecture().getId());
        }
        Path output = NativeBundleOutputPaths.forTarget(
                request.bundleName,
                request.request.target,
                request.request.webOutput,
                request.outputDirectory).get(0);
        String vcvarsall = optional(request.request.visualCppEnvironment);
        if(vcvarsall.isEmpty()) {
            vcvarsall = WindowsMSVCTarget.resolveVcvarsall();
        }
        String linker = optional(request.request.linkerExecutable);
        if(linker.isEmpty()) {
            linker = "link";
        }

        ArrayList<String> command = new ArrayList<>();
        Collections.addAll(command, "cmd", "/d", "/c", "call", vcvarsall, architecture, "&&", linker,
                "/NOLOGO", "/DLL", "/MACHINE:" + windowsMachine(request.request.target.getArchitecture()),
                "/OUT:" + output, "/IMPLIB:" + workDirectory.resolve(request.bundleName + ".lib"),
                "/PDB:" + workDirectory.resolve(request.bundleName + ".pdb"));
        for(ArchiveFile archive : archives(components)) {
            if(archive.linkMode == NativeArchiveLinkMode.WHOLE_ARCHIVE) {
                command.add("/WHOLEARCHIVE:" + archive.path);
            }
            else {
                command.add(archive.path.toString());
            }
        }
        for(String symbol : exportedSymbols(components)) {
            command.add("/EXPORT:" + symbol);
        }
        for(String library : systemLibraries(components)) {
            command.add(library.toLowerCase(Locale.ROOT).endsWith(".lib") ? library : library + ".lib");
        }
        command.addAll(linkerOptions(components));
        return singleCommandPlan("Link Windows native bundle", request.outputDirectory, command,
                request.request.environment, output);
    }

    private static NativeBundlePlan planLinux(
            ValidatedRequest request,
            List<ExtractedComponent> components,
            Path workDirectory) {
        NativeTarget.Architecture architecture = request.request.target.getArchitecture();
        if(architecture != NativeTarget.Architecture.X86
                && architecture != NativeTarget.Architecture.X86_64
                && architecture != NativeTarget.Architecture.ARMV7
                && architecture != NativeTarget.Architecture.ARM64) {
            throw new IllegalArgumentException("Unsupported Linux architecture: " + architecture.getId());
        }
        Path output = NativeBundleOutputPaths.forTarget(
                request.bundleName,
                request.request.target,
                request.request.webOutput,
                request.outputDirectory).get(0);
        String linker = optional(request.request.linkerExecutable);
        if(linker.isEmpty()) {
            linker = "g++";
        }
        ArrayList<String> command = new ArrayList<>();
        command.add(linker);
        command.add("-shared");
        if(architecture == NativeTarget.Architecture.X86) {
            command.add("-m32");
        }
        else if(architecture == NativeTarget.Architecture.X86_64) {
            command.add("-m64");
        }
        command.add("-Wl,-soname," + output.getFileName());
        command.add("-o");
        command.add(output.toString());
        addGnuArchives(command, archives(components));
        for(String library : systemLibraries(components)) {
            command.add("-l" + library);
        }
        command.addAll(linkerOptions(components));
        return singleCommandPlan("Link Linux native bundle", request.outputDirectory, command,
                request.request.environment, output);
    }

    private static NativeBundlePlan planMacOS(
            ValidatedRequest request,
            List<ExtractedComponent> components,
            Path workDirectory) {
        String architecture;
        switch(request.request.target.getArchitecture()) {
            case X86_64:
                architecture = "x86_64";
                break;
            case ARM64:
                architecture = "arm64";
                break;
            default:
                throw new IllegalArgumentException("Unsupported macOS architecture: "
                        + request.request.target.getArchitecture().getId());
        }
        Path output = NativeBundleOutputPaths.forTarget(
                request.bundleName,
                request.request.target,
                request.request.webOutput,
                request.outputDirectory).get(0);
        String linker = optional(request.request.linkerExecutable);
        if(linker.isEmpty()) {
            linker = "clang++";
        }
        ArrayList<String> command = new ArrayList<>();
        Collections.addAll(command, linker, "-dynamiclib", "-arch", architecture,
                "-mmacosx-version-min=" + request.request.minimumMacOSVersion,
                "-stdlib=libc++", "-Wl,-install_name,@rpath/" + output.getFileName(),
                "-o", output.toString());
        for(ArchiveFile archive : archives(components)) {
            if(archive.linkMode == NativeArchiveLinkMode.WHOLE_ARCHIVE) {
                command.add("-Wl,-force_load," + archive.path);
            }
            else {
                command.add(archive.path.toString());
            }
        }
        for(String library : systemLibraries(components)) {
            command.add("-l" + library);
        }
        for(String framework : frameworks(components)) {
            command.add("-framework");
            command.add(framework);
        }
        command.addAll(linkerOptions(components));
        return singleCommandPlan("Link macOS native bundle", request.outputDirectory, command,
                request.request.environment, output);
    }

    private static NativeBundlePlan planAndroid(
            ValidatedRequest request,
            List<ExtractedComponent> components,
            Path workDirectory) {
        Path ndk = resolveAndroidNdk(request.request);
        Path toolchain = resolveAndroidToolchain(ndk);
        String linker = optional(request.request.linkerExecutable);
        if(linker.isEmpty()) {
            linker = resolveTool(toolchain.resolve("bin"), "clang++").toString();
        }
        Path sysroot = toolchain.resolve("sysroot");
        if(!Files.isDirectory(sysroot)) {
            throw new IllegalArgumentException("Android NDK toolchain is missing its sysroot: " + sysroot);
        }
        String targetTriple = androidTargetTriple(request.request.target);
        Path output = NativeBundleOutputPaths.forTarget(
                request.bundleName,
                request.request.target,
                request.request.webOutput,
                request.outputDirectory).get(0);
        ArrayList<String> command = new ArrayList<>();
        Collections.addAll(command, linker,
                "--target=" + targetTriple + request.request.androidApiLevel,
                "--sysroot=" + sysroot,
                "-shared",
                "-static-libstdc++",
                "-Wl,--strip-all",
                "-Wl,-soname," + output.getFileName(),
                "-o", output.toString());
        addGnuArchives(command, archives(components));
        for(String library : systemLibraries(components)) {
            command.add("-l" + library);
        }
        command.addAll(linkerOptions(components));
        return singleCommandPlan("Link Android native bundle", request.outputDirectory, command,
                request.request.environment, output);
    }

    private static NativeBundlePlan planIOS(
            ValidatedRequest request,
            List<ExtractedComponent> components,
            Path workDirectory) {
        NativeTarget.Architecture architecture = request.request.target.getArchitecture();
        if(architecture != NativeTarget.Architecture.ARM64
                && architecture != NativeTarget.Architecture.X86_64) {
            throw new IllegalArgumentException("Unsupported iOS architecture: " + architecture.getId());
        }
        String environment = request.request.target.getEnvironment();
        String sdk;
        if(environment.equals("device")) {
            if(architecture != NativeTarget.Architecture.ARM64) {
                throw new IllegalArgumentException("iOS device bundles require arm64 architecture");
            }
            sdk = "iphoneos";
        }
        else if(environment.equals("simulator")) {
            sdk = "iphonesimulator";
        }
        else {
            throw new IllegalArgumentException("Unsupported iOS environment: " + environment);
        }
        Path output = NativeBundleOutputPaths.forTarget(
                request.bundleName,
                request.request.target,
                request.request.webOutput,
                request.outputDirectory).get(0);
        String linker = optional(request.request.linkerExecutable);
        ArrayList<String> command = new ArrayList<>();
        if(linker.isEmpty()) {
            Collections.addAll(command, "xcrun", "--sdk", sdk, "libtool");
        }
        else {
            command.add(linker);
        }
        Collections.addAll(command, "-static", "-o", output.toString());
        for(ArchiveFile archive : archives(components)) {
            command.add(archive.path.toString());
        }
        command.addAll(linkerOptions(components));
        return singleCommandPlan("Combine iOS native bundle archive", request.outputDirectory, command,
                request.request.environment, output);
    }

    private static NativeBundlePlan planWeb(
            ValidatedRequest request,
            List<ExtractedComponent> components,
            Path workDirectory) throws IOException {
        Path webDirectory = workDirectory.resolve("web");
        Files.createDirectories(webDirectory);
        Path mergedIDL = webDirectory.resolve("IDLMerged.idl");
        writeMergedIDL(components, mergedIDL);
        Path aliases = webDirectory.resolve("jparser-bundle-aliases.js");
        writeWebAliases(components, aliases);
        Path exportedFunctions = webDirectory.resolve("exported-functions.json");
        writeExportedFunctions(components, exportedFunctions);

        Path emscriptenRoot = resolveEmscriptenRoot(request.request);
        Path binder = emscriptenRoot.resolve("tools/webidl_binder.py");
        if(!Files.isRegularFile(binder)) {
            throw new IllegalArgumentException("Emscripten WebIDL binder was not found: " + binder);
        }
        String python = resolvePython(request.request);
        LinkedHashMap<String, String> webEnvironment = new LinkedHashMap<>(request.request.environment);
        String pythonPath = emscriptenRoot.toString();
        String existingPythonPath = System.getenv("PYTHONPATH");
        if(existingPythonPath != null && !existingPythonPath.trim().isEmpty()) {
            pythonPath += java.io.File.pathSeparator + existingPythonPath;
        }
        webEnvironment.put("PYTHONPATH", pythonPath);

        Path glueCpp = webDirectory.resolve("glue.cpp");
        Path glueJs = webDirectory.resolve("glue.js");
        NativeBundleCommand binderCommand = new NativeBundleCommand(
                "Generate merged WebIDL bridge",
                webDirectory,
                List.of(python, binder.toString(), mergedIDL.toString(), "glue"),
                webEnvironment,
                List.of(glueCpp, glueJs));

        String linker = optional(request.request.linkerExecutable);
        if(linker.isEmpty()) {
            linker = resolveTool(emscriptenRoot, "em++").toString();
        }
        List<Path> outputs = NativeBundleOutputPaths.forTarget(
                request.bundleName,
                request.request.target,
                request.request.webOutput,
                request.outputDirectory);
        Path outputJs = outputs.get(0);
        ArrayList<String> linkCommand = new ArrayList<>();
        linkCommand.add(linker);
        linkCommand.add(glueCpp.toString());
        for(ExtractedComponent component : components) {
            for(Path headerRoot : component.headerRoots) {
                linkCommand.add("-I" + headerRoot);
            }
        }
        linkCommand.add(request.request.buildType == NativeBuildType.DEBUG ? "-O0" : "-O3");
        Collections.addAll(linkCommand,
                "-sALLOW_MEMORY_GROWTH=1",
                "-sALLOW_TABLE_GROWTH=1",
                "-sMODULARIZE=1",
                "-sENVIRONMENT=web",
                "-sEXPORT_NAME=" + request.bundleName,
                "-sEXPORTED_FUNCTIONS=@" + exportedFunctions,
                "--post-js", glueJs.toString(),
                "--post-js", aliases.toString());
        if(request.request.webOutput == NativeWebOutput.JAVASCRIPT_AND_WASM) {
            linkCommand.add("-sWASM=1");
            linkCommand.add("-sWASM_BIGINT=1");
            if(request.request.target.getArchitecture() == NativeTarget.Architecture.WASM64) {
                linkCommand.add("-sMEMORY64=1");
            }
        }
        else {
            if(request.request.target.getArchitecture() == NativeTarget.Architecture.WASM64) {
                throw new IllegalArgumentException("JavaScript-only web output does not support wasm64");
            }
            linkCommand.add("-sWASM=0");
        }
        addGnuArchives(linkCommand, archives(components));
        for(String library : systemLibraries(components)) {
            linkCommand.add("-l" + library);
        }
        linkCommand.addAll(linkerOptions(components));
        Collections.addAll(linkCommand, "-o", outputJs.toString());

        NativeBundleCommand link = new NativeBundleCommand(
                "Link web native bundle",
                webDirectory,
                linkCommand,
                webEnvironment,
                outputs);
        return new NativeBundlePlan(List.of(binderCommand, link), outputs);
    }

    private static void writeMergedIDL(List<ExtractedComponent> components, Path output) throws IOException {
        StringBuilder merged = new StringBuilder();
        LinkedHashMap<String, String> declarations = new LinkedHashMap<>();
        for(ExtractedComponent component : components) {
            for(Path idlFile : component.idlFiles) {
                String content = Files.readString(idlFile, StandardCharsets.UTF_8);
                Matcher matcher = WEB_DECLARATION.matcher(content);
                while(matcher.find()) {
                    if(matcher.group(1) != null) {
                        continue;
                    }
                    String declaration = matcher.group(2);
                    String owner = component.input.manifest.getComponentId() + ":" + idlFile.getFileName();
                    String previous = declarations.put(declaration, owner);
                    if(previous != null) {
                        throw new IllegalArgumentException("Duplicate WebIDL declaration " + declaration
                                + " in " + previous + " and " + owner);
                    }
                }
                merged.append("\n// ")
                        .append(component.input.manifest.getComponentId())
                        .append('/')
                        .append(idlFile.getFileName())
                        .append("\n\n")
                        .append(content);
                if(!content.endsWith("\n")) {
                    merged.append('\n');
                }
            }
        }
        if(merged.length() == 0) {
            throw new IllegalArgumentException("Web native bundle has no WebIDL inputs");
        }
        Files.writeString(output, merged.toString(), StandardCharsets.UTF_8);
    }

    private static void writeWebAliases(List<ExtractedComponent> components, Path output) throws IOException {
        StringBuilder aliases = new StringBuilder();
        aliases.append("\n(function(scope) {\n");
        for(ExtractedComponent component : components) {
            aliases.append("  scope[")
                    .append(jsonString(component.input.manifest.getWebModuleName()))
                    .append("] = Module;\n");
        }
        aliases.append("})(typeof globalThis !== 'undefined' ? globalThis : "
                + "(typeof window !== 'undefined' ? window : this));\n");
        Files.writeString(output, aliases.toString(), StandardCharsets.UTF_8);
    }

    private static void writeExportedFunctions(List<ExtractedComponent> components, Path output)
            throws IOException {
        LinkedHashSet<String> symbols = new LinkedHashSet<>();
        symbols.add("_malloc");
        symbols.add("_free");
        for(String symbol : exportedSymbols(components)) {
            symbols.add(symbol.startsWith("_") ? symbol : "_" + symbol);
        }
        StringBuilder json = new StringBuilder("[");
        boolean first = true;
        for(String symbol : symbols) {
            if(!first) {
                json.append(',');
            }
            json.append(jsonString(symbol));
            first = false;
        }
        json.append(']');
        Files.writeString(output, json.toString(), StandardCharsets.UTF_8);
    }

    private static NativeBundlePlan singleCommandPlan(
            String description,
            Path workingDirectory,
            List<String> arguments,
            Map<String, String> environment,
            Path output) {
        NativeBundleCommand command = new NativeBundleCommand(
                description,
                workingDirectory,
                arguments,
                environment,
                List.of(output));
        return new NativeBundlePlan(List.of(command), List.of(output));
    }

    private static void addGnuArchives(List<String> command, List<ArchiveFile> archives) {
        command.add("-Wl,--start-group");
        for(ArchiveFile archive : archives) {
            if(archive.linkMode == NativeArchiveLinkMode.WHOLE_ARCHIVE) {
                command.add("-Wl,--whole-archive");
                command.add(archive.path.toString());
                command.add("-Wl,--no-whole-archive");
            }
            else {
                command.add(archive.path.toString());
            }
        }
        command.add("-Wl,--end-group");
    }

    private static ArrayList<ArchiveFile> archives(List<ExtractedComponent> components) {
        ArrayList<ArchiveFile> result = new ArrayList<>();
        for(ExtractedComponent component : components) {
            result.addAll(component.archives);
        }
        return result;
    }

    private static ArrayList<String> systemLibraries(List<ExtractedComponent> components) {
        LinkedHashSet<String> result = new LinkedHashSet<>();
        for(ExtractedComponent component : components) {
            result.addAll(component.input.manifest.getSystemLibraries());
        }
        return new ArrayList<>(result);
    }

    private static ArrayList<String> frameworks(List<ExtractedComponent> components) {
        LinkedHashSet<String> result = new LinkedHashSet<>();
        for(ExtractedComponent component : components) {
            result.addAll(component.input.manifest.getFrameworks());
        }
        return new ArrayList<>(result);
    }

    private static ArrayList<String> linkerOptions(List<ExtractedComponent> components) {
        LinkedHashSet<String> result = new LinkedHashSet<>();
        for(ExtractedComponent component : components) {
            result.addAll(component.input.manifest.getLinkerOptions());
        }
        return new ArrayList<>(result);
    }

    private static ArrayList<String> exportedSymbols(List<ExtractedComponent> components) {
        ArrayList<String> result = new ArrayList<>();
        for(ExtractedComponent component : components) {
            result.addAll(component.input.manifest.getExportedSymbols());
        }
        return result;
    }

    private static String windowsMachine(NativeTarget.Architecture architecture) {
        switch(architecture) {
            case X86:
                return "X86";
            case X86_64:
                return "X64";
            case ARM64:
                return "ARM64";
            default:
                throw new IllegalArgumentException("Unsupported Windows architecture: " + architecture.getId());
        }
    }

    private static String androidTargetTriple(NativeTarget target) {
        String abi = target.getAbi();
        switch(abi) {
            case "arm64-v8a":
                requireArchitecture(target, NativeTarget.Architecture.ARM64);
                return "aarch64-linux-android";
            case "armeabi-v7a":
                requireArchitecture(target, NativeTarget.Architecture.ARMV7);
                return "armv7a-linux-androideabi";
            case "x86-64":
            case "x86_64":
                requireArchitecture(target, NativeTarget.Architecture.X86_64);
                return "x86_64-linux-android";
            case "x86":
                requireArchitecture(target, NativeTarget.Architecture.X86);
                return "i686-linux-android";
            default:
                throw new IllegalArgumentException("Unsupported Android ABI: " + abi);
        }
    }

    private static void requireArchitecture(NativeTarget target, NativeTarget.Architecture expected) {
        if(target.getArchitecture() != expected) {
            throw new IllegalArgumentException("Android ABI " + target.getAbi() + " requires "
                    + expected.getId() + " architecture");
        }
    }

    private static Path resolveAndroidNdk(NativeBundleRequest request) {
        String configured = optional(request.androidNdkHome);
        if(configured.isEmpty()) {
            configured = firstNonBlank(
                    System.getProperty("jparser.androidNdkHome"),
                    System.getenv("ANDROID_NDK_HOME"),
                    System.getenv("ANDROID_NDK_ROOT"));
        }
        if(configured.isEmpty()) {
            String sdk = firstNonBlank(System.getenv("ANDROID_SDK_ROOT"), System.getenv("ANDROID_HOME"));
            if(!sdk.isEmpty()) {
                Path ndkRoot = Path.of(sdk).resolve("ndk");
                if(Files.isDirectory(ndkRoot)) {
                    try(Stream<Path> stream = Files.list(ndkRoot)) {
                        configured = stream.filter(Files::isDirectory)
                                .sorted(Comparator.comparing(
                                        (Path path) -> path.getFileName().toString()).reversed())
                                .map(Path::toString)
                                .findFirst()
                                .orElse("");
                    }
                    catch(IOException exception) {
                        throw new IllegalArgumentException("Unable to inspect Android NDK directory: " + ndkRoot,
                                exception);
                    }
                }
                if(configured.isEmpty()) {
                    Path legacy = Path.of(sdk).resolve("ndk-bundle");
                    if(Files.isDirectory(legacy)) {
                        configured = legacy.toString();
                    }
                }
            }
        }
        if(configured.isEmpty()) {
            throw new IllegalArgumentException("Android NDK not found. Set NativeBundleRequest.androidNdkHome, "
                    + "ANDROID_NDK_HOME, or ANDROID_NDK_ROOT");
        }
        Path ndk = Path.of(configured).toAbsolutePath().normalize();
        if(!Files.isDirectory(ndk)) {
            throw new IllegalArgumentException("Android NDK directory does not exist: " + ndk);
        }
        return ndk;
    }

    private static Path resolveAndroidToolchain(Path ndk) {
        Path prebuilt = ndk.resolve("toolchains/llvm/prebuilt");
        if(!Files.isDirectory(prebuilt)) {
            throw new IllegalArgumentException("Android NDK has no LLVM prebuilt toolchain: " + ndk);
        }
        String osName = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        ArrayList<String> candidates = new ArrayList<>();
        if(osName.contains("win")) {
            candidates.add("windows-x86_64");
        }
        else if(osName.contains("mac")) {
            candidates.add("darwin-x86_64");
            candidates.add("darwin-arm64");
        }
        else {
            candidates.add("linux-x86_64");
        }
        for(String candidate : candidates) {
            Path toolchain = prebuilt.resolve(candidate);
            if(Files.isDirectory(toolchain)) {
                return toolchain;
            }
        }
        try(Stream<Path> stream = Files.list(prebuilt)) {
            return stream.filter(Files::isDirectory)
                    .sorted(Comparator.comparing(Path::toString))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Android NDK contains no host toolchain: " + prebuilt));
        }
        catch(IOException exception) {
            throw new IllegalArgumentException("Unable to inspect Android NDK toolchains: " + prebuilt,
                    exception);
        }
    }

    private static Path resolveEmscriptenRoot(NativeBundleRequest request) {
        String configured = optional(request.emscriptenRoot);
        if(configured.isEmpty()) {
            configured = firstNonBlank(System.getProperty("jparser.emscriptenRoot"),
                    System.getenv("EMSCRIPTEN"));
        }
        if(configured.isEmpty()) {
            String emsdk = firstNonBlank(System.getenv("EMSDK"));
            if(!emsdk.isEmpty()) {
                configured = Path.of(emsdk, "upstream", "emscripten").toString();
            }
        }
        if(configured.isEmpty()) {
            throw new IllegalArgumentException("Emscripten was not found. Set "
                    + "NativeBundleRequest.emscriptenRoot, EMSCRIPTEN, or EMSDK");
        }
        Path root = Path.of(configured).toAbsolutePath().normalize();
        if(!Files.isDirectory(root)) {
            throw new IllegalArgumentException("Emscripten directory does not exist: " + root);
        }
        return root;
    }

    private static String resolvePython(NativeBundleRequest request) {
        String configured = optional(request.pythonExecutable);
        if(configured.isEmpty()) {
            configured = firstNonBlank(System.getenv("EMSDK_PYTHON"));
        }
        if(!configured.isEmpty()) {
            return configured;
        }
        String osName = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        return osName.contains("win") ? "python" : "python3";
    }

    private static Path resolveTool(Path directory, String name) {
        ArrayList<String> names = new ArrayList<>();
        String osName = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        if(osName.contains("win")) {
            names.add(name + ".exe");
            names.add(name + ".cmd");
            names.add(name + ".bat");
        }
        names.add(name);
        for(String candidate : names) {
            Path path = directory.resolve(candidate);
            if(Files.isRegularFile(path)) {
                return path.toAbsolutePath().normalize();
            }
        }
        throw new IllegalArgumentException("Required native tool was not found below " + directory + ": " + name);
    }

    private static void validateNativeName(String description, String value, ComponentInput component) {
        if(!NATIVE_NAME.matcher(value).matches()) {
            throw new IllegalArgumentException("Invalid " + description + " " + value + " in " + component.jar);
        }
    }

    private static void validateLinkerOption(String option, ComponentInput component) {
        String value = required("linker option", option);
        if(containsControlOrWhitespace(value)) {
            throw new IllegalArgumentException("Linker option must be one argument without control characters: "
                    + value + " in " + component.jar);
        }
        String lower = value.toLowerCase(Locale.ROOT);
        String[] forbiddenPrefixes = {
                "@", "-o", "--output", "-wl,-o", "-wl,--output",
                "/out", "/implib", "/pdb", "/def", "/libpath",
                "-l", "--library-path", "--sysroot", "-isysroot",
                "-wl,-rpath", "-wl,--rpath", "-wl,-rpath-link", "-wl,--dynamic-linker"
        };
        for(String prefix : forbiddenPrefixes) {
            if(lower.equals(prefix)
                    || lower.startsWith(prefix + "=")
                    || lower.startsWith(prefix + ":")
                    || lower.startsWith(prefix + ",")) {
                throw new IllegalArgumentException("Linker option may not select files or outputs: " + value
                        + " in " + component.jar);
            }
        }
        if(value.indexOf('\\') >= 0
                || (value.indexOf('/') >= 0 && !value.startsWith("/"))
                || value.endsWith(".a")
                || lower.endsWith(".lib")
                || lower.endsWith(".so")
                || lower.endsWith(".dylib")
                || lower.endsWith(".dll")) {
            throw new IllegalArgumentException("Linker option may not contain a native file path: " + value
                    + " in " + component.jar);
        }
    }

    private static boolean containsControlOrWhitespace(String value) {
        for(int i = 0; i < value.length(); i++) {
            char character = value.charAt(i);
            if(Character.isWhitespace(character) || Character.isISOControl(character)) {
                return true;
            }
        }
        return false;
    }

    private static void validateEnvironment(Map<String, String> environment) {
        for(Map.Entry<String, String> entry : environment.entrySet()) {
            String name = entry.getKey();
            String value = entry.getValue();
            if(name == null || !name.matches("[A-Za-z_][A-Za-z0-9_]*")) {
                throw new IllegalArgumentException("Invalid native bundle environment variable name: " + name);
            }
            if(value == null || value.indexOf('\0') >= 0) {
                throw new IllegalArgumentException("Invalid native bundle environment variable value for " + name);
            }
        }
    }

    private static void validateOptionalCommand(String name, String value) {
        if(value != null && (value.indexOf('\0') >= 0 || value.indexOf('\n') >= 0 || value.indexOf('\r') >= 0)) {
            throw new IllegalArgumentException(name + " contains a control character");
        }
    }

    private static void validateVersion(String name, String value) {
        String normalized = required(name, value);
        if(!normalized.matches("[0-9]+(?:\\.[0-9]+){0,3}")) {
            throw new IllegalArgumentException(name + " must be a numeric dotted version: " + value);
        }
    }

    private static int compareVersions(String left, String right) {
        String[] leftParts = left.split("\\.");
        String[] rightParts = right.split("\\.");
        int count = Math.max(leftParts.length, rightParts.length);
        for(int i = 0; i < count; i++) {
            BigInteger leftPart = i < leftParts.length
                    ? new BigInteger(leftParts[i])
                    : BigInteger.ZERO;
            BigInteger rightPart = i < rightParts.length
                    ? new BigInteger(rightParts[i])
                    : BigInteger.ZERO;
            int comparison = leftPart.compareTo(rightPart);
            if(comparison != 0) {
                return comparison;
            }
        }
        return 0;
    }

    private static void requireEqual(
            ComponentInput reference,
            ComponentInput component,
            String description,
            String expected,
            String actual) {
        if(!expected.equals(actual)) {
            throw new IllegalArgumentException("Incompatible " + description + " between native components "
                    + reference.manifest.getComponentId() + " (" + printable(expected) + ") and "
                    + component.manifest.getComponentId() + " (" + printable(actual) + ")");
        }
    }

    private static void requireCompatibleOptional(
            List<ComponentInput> components,
            String description,
            Function<NativeComponentManifest, String> value) {
        ComponentInput reference = null;
        String expected = "";
        for(ComponentInput component : components) {
            String actual = value.apply(component.manifest);
            if(actual.isEmpty()) {
                continue;
            }
            if(reference == null) {
                reference = component;
                expected = actual;
            }
            else if(!expected.equals(actual)) {
                throw new IllegalArgumentException("Incompatible " + description
                        + " between native components "
                        + reference.manifest.getComponentId() + " (" + expected + ") and "
                        + component.manifest.getComponentId() + " (" + actual + ")");
            }
        }
    }

    private static IllegalArgumentException incompatible(
            ComponentInput component,
            String description,
            String expected,
            String actual) {
        return new IllegalArgumentException("Incompatible " + description + " in native component "
                + component.manifest.getComponentId() + ": expected " + expected + " but found " + actual);
    }

    private static String printable(String value) {
        return value.isEmpty() ? "<unspecified>" : value;
    }

    private static String required(String name, String value) {
        if(value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        if(value.indexOf('\0') >= 0 || value.indexOf('\n') >= 0 || value.indexOf('\r') >= 0) {
            throw new IllegalArgumentException(name + " contains a control character");
        }
        return value.trim();
    }

    private static String optional(String value) {
        return value == null ? "" : value.trim();
    }

    private static String firstNonBlank(String... values) {
        for(String value : values) {
            if(value != null && !value.trim().isEmpty()) {
                return value.trim();
            }
        }
        return "";
    }

    private static String jsonString(String value) {
        StringBuilder result = new StringBuilder("\"");
        for(int i = 0; i < value.length(); i++) {
            char character = value.charAt(i);
            switch(character) {
                case '\\':
                    result.append("\\\\");
                    break;
                case '"':
                    result.append("\\\"");
                    break;
                case '\n':
                    result.append("\\n");
                    break;
                case '\r':
                    result.append("\\r");
                    break;
                case '\t':
                    result.append("\\t");
                    break;
                default:
                    if(character < 0x20) {
                        result.append(String.format(Locale.ROOT, "\\u%04x", (int)character));
                    }
                    else {
                        result.append(character);
                    }
                    break;
            }
        }
        return result.append('"').toString();
    }

    private static void deleteTemporaryDirectory(Path directory, Path outputDirectory) throws IOException {
        Path normalizedDirectory = directory.toAbsolutePath().normalize();
        Path normalizedOutput = outputDirectory.toAbsolutePath().normalize();
        if(!normalizedDirectory.startsWith(normalizedOutput)
                || normalizedDirectory.equals(normalizedOutput)
                || normalizedDirectory.getFileName() == null
                || !normalizedDirectory.getFileName().toString().startsWith(".jparser-bundle-")) {
            throw new IllegalStateException("Refusing to delete unexpected native bundle directory: "
                    + normalizedDirectory);
        }
        if(!Files.exists(normalizedDirectory)) {
            return;
        }
        try(Stream<Path> stream = Files.walk(normalizedDirectory)) {
            List<Path> paths = stream.sorted(Comparator.reverseOrder()).toList();
            IOException failure = null;
            for(Path path : paths) {
                try {
                    Files.deleteIfExists(path);
                }
                catch(IOException exception) {
                    if(failure == null) {
                        failure = exception;
                    }
                    else {
                        failure.addSuppressed(exception);
                    }
                }
            }
            if(failure != null) {
                throw failure;
            }
        }
    }

    private static final class ValidatedRequest {
        private final NativeBundleRequest request;
        private final String bundleName;
        private final Path outputDirectory;
        private final List<ComponentInput> components;

        private ValidatedRequest(
                NativeBundleRequest request,
                String bundleName,
                Path outputDirectory,
                List<ComponentInput> components) {
            this.request = request;
            this.bundleName = bundleName;
            this.outputDirectory = outputDirectory;
            this.components = components;
        }
    }

    private static final class ComponentInput {
        private final Path jar;
        private final NativeComponentManifest manifest;

        private ComponentInput(Path jar, NativeComponentManifest manifest) {
            this.jar = jar;
            this.manifest = manifest;
        }
    }

    private static final class ExtractedComponent {
        private final ComponentInput input;
        private final Path root;
        private final List<ArchiveFile> archives;
        private final List<Path> idlFiles;
        private final List<Path> headerRoots;

        private ExtractedComponent(
                ComponentInput input,
                Path root,
                List<ArchiveFile> archives,
                List<Path> idlFiles,
                List<Path> headerRoots) {
            this.input = input;
            this.root = root;
            this.archives = archives;
            this.idlFiles = idlFiles;
            this.headerRoots = headerRoots;
        }
    }

    private static final class ArchiveFile {
        private final Path path;
        private final NativeArchiveLinkMode linkMode;

        private ArchiveFile(Path path, NativeArchiveLinkMode linkMode) {
            this.path = path;
            this.linkMode = linkMode;
        }
    }

    private static final class NativeBundlePlan {
        private final List<NativeBundleCommand> commands;
        private final List<Path> outputs;

        private NativeBundlePlan(List<NativeBundleCommand> commands, List<Path> outputs) {
            this.commands = commands;
            this.outputs = outputs;
        }
    }
}
