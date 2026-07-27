import com.github.xpenatan.jParser.builder.bundle.NativeBridge;
import com.github.xpenatan.jParser.builder.bundle.NativeBundleBuilder;
import com.github.xpenatan.jParser.builder.bundle.NativeBundleRequest;
import com.github.xpenatan.jParser.builder.bundle.NativeBundleResult;
import com.github.xpenatan.jParser.builder.bundle.NativeComponentBuilder;
import com.github.xpenatan.jParser.builder.bundle.NativeComponentRequest;
import com.github.xpenatan.jParser.builder.bundle.NativeComponentRole;
import com.github.xpenatan.jParser.builder.bundle.NativeTarget;
import com.github.xpenatan.jParser.builder.targets.WindowsMSVCTarget;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Host smoke fixture for application-owned JNI-only and mixed JNI/FFM bundles.
 */
public final class BuildSharedLibBundle {
    private static final String COMPONENT_VERSION = "fixture";
    private static final String RUNTIME_ABI = "jparser-runtime-1";

    private BuildSharedLibBundle() {
    }

    public static void main(String[] args) throws Exception {
        if(args.length != 3) {
            throw new IllegalArgumentException(
                    "Expected <repository-root> <jni|mixed> <output-directory>");
        }
        Path repositoryRoot = Path.of(args[0]).toAbsolutePath().normalize();
        String mode = args[1].trim().toLowerCase(Locale.ROOT);
        if(!mode.equals("jni") && !mode.equals("mixed")) {
            throw new IllegalArgumentException("Unsupported SharedLib bundle mode: " + mode);
        }
        Path outputDirectory = Path.of(args[2]).toAbsolutePath().normalize();
        Files.createDirectories(outputDirectory);

        Host host = Host.current();
        NativeBridge runtimeBridge = mode.equals("mixed") ? NativeBridge.FFM : NativeBridge.JNI;
        NativeBridge libABridge = NativeBridge.JNI;
        NativeBridge libBBridge = mode.equals("mixed") ? NativeBridge.FFM : NativeBridge.JNI;
        Path resourcesDirectory = outputDirectory.resolve("resources");

        ArrayList<Path> resourceJars = new ArrayList<>();
        resourceJars.add(component(
                repositoryRoot,
                resourcesDirectory,
                host,
                "runtime",
                NativeComponentRole.RUNTIME,
                runtimeBridge));
        resourceJars.add(component(
                repositoryRoot,
                resourcesDirectory,
                host,
                "LibA",
                NativeComponentRole.BINDING,
                libABridge));
        resourceJars.add(component(
                repositoryRoot,
                resourcesDirectory,
                host,
                "LibB",
                NativeComponentRole.BINDING,
                libBBridge));

        NativeBundleRequest request = new NativeBundleRequest();
        request.bundleName = mode.equals("mixed") ? "SharedLibFatMixed" : "SharedLibFatJni";
        request.outputDirectory = outputDirectory;
        request.target = host.target;
        request.componentJars.addAll(resourceJars);
        NativeBundleResult result = NativeBundleBuilder.build(request);
        verifyNoStandaloneJParserDependencies(host, result.getOutputPaths());
        for(Path output : result.getOutputPaths()) {
            System.out.println("SHAREDLIB_FAT_OUTPUT=" + output);
        }
    }

    private static Path component(
            Path repositoryRoot,
            Path resourcesDirectory,
            Host host,
            String componentId,
            NativeComponentRole role,
            NativeBridge bridge) throws IOException {
        NativeComponentRequest request = new NativeComponentRequest();
        request.outputJar = resourcesDirectory.resolve(
                componentId + "_resources-" + host.target.getClassifierPrefix()
                        + "-" + bridge.getId() + ".jar");
        request.componentId = componentId;
        request.componentVersion = COMPONENT_VERSION;
        request.role = role;
        request.target = host.target;
        request.bridge = bridge;
        request.minimumJavaVersion = bridge == NativeBridge.FFM ? 25 : 8;
        request.runtimeAbi = RUNTIME_ABI;
        request.toolchainId = host.toolchainId;
        request.cRuntime = host.cRuntime;
        request.cppRuntime = host.cppRuntime;
        request.implementationArchive = archive(
                repositoryRoot,
                host,
                componentId,
                bridge,
                false);
        request.bridgeArchive = archive(
                repositoryRoot,
                host,
                componentId,
                bridge,
                true);
        request.licenseFiles.add(repositoryRoot.resolve("LICENSE"));
        return NativeComponentBuilder.build(request).getPath();
    }

    private static Path archive(
            Path repositoryRoot,
            Host host,
            String componentId,
            NativeBridge bridge,
            boolean bridgeArchive) {
        String api = bridge.getId();
        String libraryName = bridgeArchive ? componentId + "_bridge" : componentId;
        String relativeRoot;
        if(componentId.equals("runtime")) {
            relativeRoot = "jParser/runtime/builder/build/c++/libs/";
            libraryName = bridgeArchive ? "runtime_bridge" : "runtime";
        }
        else {
            String moduleDirectory = componentId.equals("LibA") ? "libA" : "libB";
            relativeRoot = "examples/SharedLib/" + moduleDirectory
                    + "/builder/build/c++/libs/";
        }

        String relative;
        switch(host.target.getOperatingSystem()) {
            case WINDOWS:
                relative = "windows/vc/" + api + "/" + libraryName + "64_.lib";
                break;
            case LINUX:
                relative = "linux/" + api + "/lib" + libraryName + "64_.a";
                break;
            case MACOS:
                String architectureDirectory =
                        host.target.getArchitecture() == NativeTarget.Architecture.ARM64
                                ? "mac/arm/" : "mac/";
                relative = architectureDirectory + api + "/lib" + libraryName + "64_.a";
                break;
            default:
                throw new IllegalArgumentException(
                        "SharedLib desktop fixture does not support " + host.target);
        }
        return repositoryRoot.resolve(relativeRoot).resolve(relative);
    }

    private static void verifyNoStandaloneJParserDependencies(
            Host host,
            List<Path> outputs) throws Exception {
        if(outputs.size() != 1) {
            throw new IllegalStateException("Desktop SharedLib bundle must have exactly one output");
        }
        Path library = outputs.get(0);
        ArrayList<String> command = new ArrayList<>();
        switch(host.target.getOperatingSystem()) {
            case WINDOWS:
                command.add("cmd");
                command.add("/d");
                command.add("/c");
                command.add("call");
                command.add(WindowsMSVCTarget.resolveVcvarsall());
                command.add("x64");
                command.add("&&");
                command.add("dumpbin");
                command.add("/dependents");
                command.add(library.toString());
                break;
            case LINUX:
                command.add("ldd");
                command.add(library.toString());
                break;
            case MACOS:
                command.add("otool");
                command.add("-L");
                command.add(library.toString());
                break;
            default:
                throw new IllegalArgumentException("Unsupported desktop host: " + host.target);
        }
        Process process = new ProcessBuilder(command)
                .redirectErrorStream(true)
                .start();
        String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        int exitCode = process.waitFor();
        if(exitCode != 0) {
            throw new IllegalStateException(
                    "Native dependency inspection failed with exit code " + exitCode + ":\n" + output);
        }
        String normalized = output.toLowerCase(Locale.ROOT);
        for(String standalone : List.of(
                "liba64.dll",
                "libb64.dll",
                "runtime64.dll",
                "libliba64.so",
                "liblibb64.so",
                "libruntime64.so",
                "libliba64.dylib",
                "liblibb64.dylib",
                "libruntime64.dylib")) {
            if(normalized.contains(standalone)) {
                throw new IllegalStateException(
                        "Fat bundle still depends on standalone native library "
                                + standalone + ":\n" + output);
            }
        }
        System.out.println(output);
    }

    private static final class Host {
        private final NativeTarget target;
        private final String toolchainId;
        private final String cRuntime;
        private final String cppRuntime;

        private Host(
                NativeTarget target,
                String toolchainId,
                String cRuntime,
                String cppRuntime) {
            this.target = target;
            this.toolchainId = toolchainId;
            this.cRuntime = cRuntime;
            this.cppRuntime = cppRuntime;
        }

        private static Host current() {
            String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
            String architecture =
                    System.getProperty("os.arch", "").toLowerCase(Locale.ROOT);
            if(os.contains("win")) {
                return new Host(
                        NativeTarget.of(
                                NativeTarget.OperatingSystem.WINDOWS,
                                NativeTarget.Architecture.X86_64),
                        "msvc",
                        "msvc-md",
                        "msvc");
            }
            if(os.contains("linux")
                    && (architecture.equals("x86_64") || architecture.equals("amd64"))) {
                return new Host(
                        NativeTarget.of(
                                NativeTarget.OperatingSystem.LINUX,
                                NativeTarget.Architecture.X86_64),
                        "gcc",
                        "glibc",
                        "libstdc++");
            }
            if(os.contains("mac")) {
                NativeTarget.Architecture targetArchitecture =
                        architecture.equals("aarch64") || architecture.equals("arm64")
                                ? NativeTarget.Architecture.ARM64
                                : NativeTarget.Architecture.X86_64;
                return new Host(
                        NativeTarget.of(
                                NativeTarget.OperatingSystem.MACOS,
                                targetArchitecture),
                        "apple-clang",
                        "libsystem",
                        "libc++");
            }
            throw new IllegalStateException(
                    "Unsupported SharedLib fat-bundle host: " + os + "/" + architecture);
        }
    }
}
