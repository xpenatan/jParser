import com.github.xpenatan.jParser.builder.bundle.NativeBridge;
import com.github.xpenatan.jParser.builder.bundle.NativeComponentBuilder;
import com.github.xpenatan.jParser.builder.bundle.NativeComponentRequest;
import com.github.xpenatan.jParser.builder.bundle.NativeComponentRole;
import com.github.xpenatan.jParser.builder.bundle.NativeResourcesPublicationRequest;
import com.github.xpenatan.jParser.builder.bundle.NativeResourcesPublicationVerifier;
import com.github.xpenatan.jParser.builder.bundle.NativeTarget;
import java.nio.file.Path;
import java.util.Arrays;

/**
 * Repository release helper for the {@code runtime_resources} coordinate.
 *
 * <p>The reusable packaging and verification behavior remains in the public
 * {@code builder.bundle} API; this class only maps jParser's own runtime output
 * matrix to that API.</p>
 */
public final class BuildRuntimeResources {
    private static final String COMPONENT_ID = "runtime";
    private static final String RUNTIME_ABI = "jparser-runtime-1";

    private BuildRuntimeResources() {
    }

    public static void main(String[] args) throws Exception {
        if(args.length == 0) {
            throw new IllegalArgumentException("Expected build or verify command");
        }
        if(args[0].equals("build")) {
            if(args.length != 5) {
                throw new IllegalArgumentException(
                        "build requires: <repository-root> <variant> <output-jar> <version>");
            }
            build(
                    Path.of(args[1]),
                    RuntimeVariant.fromId(args[2]),
                    Path.of(args[3]),
                    args[4]);
            return;
        }
        if(args[0].equals("verify")) {
            if(args.length < 5) {
                throw new IllegalArgumentException(
                        "verify requires: <version> <complete> <declared-csv> <resource-jar>...");
            }
            verify(args);
            return;
        }
        throw new IllegalArgumentException("Unsupported runtime resources command: " + args[0]);
    }

    private static void build(
            Path repositoryRoot,
            RuntimeVariant variant,
            Path outputJar,
            String version) throws Exception {
        Path root = repositoryRoot.toAbsolutePath().normalize();
        NativeComponentRequest request = new NativeComponentRequest();
        request.outputJar = outputJar;
        request.componentId = COMPONENT_ID;
        request.componentVersion = version;
        request.role = NativeComponentRole.RUNTIME;
        request.target = variant.target;
        request.bridge = variant.bridge;
        request.minimumJavaVersion = variant.bridge == NativeBridge.FFM ? 25 : 8;
        request.minimumPlatformVersion = variant.minimumPlatformVersion;
        request.runtimeAbi = RUNTIME_ABI;
        request.toolchainId = variant.toolchainId;
        request.cRuntime = variant.cRuntime;
        request.cppRuntime = variant.cppRuntime;
        request.implementationArchive = root.resolve(variant.implementationPath);
        if(variant.bridgePath != null) {
            request.bridgeArchive = root.resolve(variant.bridgePath);
        }
        request.licenseFiles.add(root.resolve("LICENSE"));

        if(variant.bridge == NativeBridge.WEB) {
            request.webModuleName = COMPONENT_ID;
            request.webIDLFiles.add(root.resolve(
                    "jParser/runtime/base/src/main/resources/RuntimeHelper.idl"));
            request.webHeaderDirectories.add(root.resolve(
                    "jParser/runtime/base/src/main/resources"));
            request.webHeaderDirectories.add(root.resolve(
                    "jParser/runtime/builder/src/main/cpp/custom"));
        }
        NativeComponentBuilder.build(request);
    }

    private static void verify(String[] args) throws Exception {
        NativeResourcesPublicationRequest request = new NativeResourcesPublicationRequest();
        request.componentId = COMPONENT_ID;
        request.componentVersion = args[1];
        request.requireCompleteMatrix = Boolean.parseBoolean(args[2]);
        if(!args[3].isEmpty()) {
            request.declaredClassifiers.addAll(Arrays.asList(args[3].split(",")));
        }
        for(int i = 4; i < args.length; i++) {
            request.resourceJars.add(Path.of(args[i]));
        }
        NativeResourcesPublicationVerifier.verify(request);
    }

    private enum RuntimeVariant {
        WINDOWS_X86_64_JNI(
                "windows-x86_64-jni",
                NativeTarget.of(NativeTarget.OperatingSystem.WINDOWS, NativeTarget.Architecture.X86_64),
                NativeBridge.JNI,
                "jParser/runtime/builder/build/c++/libs/windows/vc/jni/runtime64_.lib",
                "jParser/runtime/builder/build/c++/libs/windows/vc/jni/runtime_bridge64_.lib",
                "", "msvc", "msvc-md", "msvc"),
        WINDOWS_X86_64_FFM(
                "windows-x86_64-ffm",
                NativeTarget.of(NativeTarget.OperatingSystem.WINDOWS, NativeTarget.Architecture.X86_64),
                NativeBridge.FFM,
                "jParser/runtime/builder/build/c++/libs/windows/vc/ffm/runtime64_.lib",
                "jParser/runtime/builder/build/c++/libs/windows/vc/ffm/runtime_bridge64_.lib",
                "", "msvc", "msvc-md", "msvc"),
        LINUX_X86_64_JNI(
                "linux-x86_64-jni",
                NativeTarget.of(NativeTarget.OperatingSystem.LINUX, NativeTarget.Architecture.X86_64),
                NativeBridge.JNI,
                "jParser/runtime/builder/build/c++/libs/linux/jni/libruntime64_.a",
                "jParser/runtime/builder/build/c++/libs/linux/jni/libruntime_bridge64_.a",
                "", "gcc", "glibc", "libstdc++"),
        LINUX_X86_64_FFM(
                "linux-x86_64-ffm",
                NativeTarget.of(NativeTarget.OperatingSystem.LINUX, NativeTarget.Architecture.X86_64),
                NativeBridge.FFM,
                "jParser/runtime/builder/build/c++/libs/linux/ffm/libruntime64_.a",
                "jParser/runtime/builder/build/c++/libs/linux/ffm/libruntime_bridge64_.a",
                "", "gcc", "glibc", "libstdc++"),
        MACOS_X86_64_JNI(
                "macos-x86_64-jni",
                NativeTarget.of(NativeTarget.OperatingSystem.MACOS, NativeTarget.Architecture.X86_64),
                NativeBridge.JNI,
                "jParser/runtime/builder/build/c++/libs/mac/jni/libruntime64_.a",
                "jParser/runtime/builder/build/c++/libs/mac/jni/libruntime_bridge64_.a",
                "10.13", "apple-clang", "libsystem", "libc++"),
        MACOS_X86_64_FFM(
                "macos-x86_64-ffm",
                NativeTarget.of(NativeTarget.OperatingSystem.MACOS, NativeTarget.Architecture.X86_64),
                NativeBridge.FFM,
                "jParser/runtime/builder/build/c++/libs/mac/ffm/libruntime64_.a",
                "jParser/runtime/builder/build/c++/libs/mac/ffm/libruntime_bridge64_.a",
                "10.13", "apple-clang", "libsystem", "libc++"),
        MACOS_ARM64_JNI(
                "macos-arm64-jni",
                NativeTarget.of(NativeTarget.OperatingSystem.MACOS, NativeTarget.Architecture.ARM64),
                NativeBridge.JNI,
                "jParser/runtime/builder/build/c++/libs/mac/arm/jni/libruntime64_.a",
                "jParser/runtime/builder/build/c++/libs/mac/arm/jni/libruntime_bridge64_.a",
                "10.13", "apple-clang", "libsystem", "libc++"),
        MACOS_ARM64_FFM(
                "macos-arm64-ffm",
                NativeTarget.of(NativeTarget.OperatingSystem.MACOS, NativeTarget.Architecture.ARM64),
                NativeBridge.FFM,
                "jParser/runtime/builder/build/c++/libs/mac/arm/ffm/libruntime64_.a",
                "jParser/runtime/builder/build/c++/libs/mac/arm/ffm/libruntime_bridge64_.a",
                "10.13", "apple-clang", "libsystem", "libc++"),
        ANDROID_X86_JNI(
                "android-x86-jni",
                NativeTarget.android(NativeTarget.Architecture.X86, "x86"),
                NativeBridge.JNI,
                "jParser/runtime/builder/build/c++/libs/android/x86/libruntime.a",
                "jParser/runtime/builder/build/c++/libs/android/x86/libruntime_bridge.a",
                "29", "android-ndk", "bionic", "libc++"),
        ANDROID_X86_64_JNI(
                "android-x86_64-jni",
                NativeTarget.android(NativeTarget.Architecture.X86_64, "x86_64"),
                NativeBridge.JNI,
                "jParser/runtime/builder/build/c++/libs/android/x86_64/libruntime.a",
                "jParser/runtime/builder/build/c++/libs/android/x86_64/libruntime_bridge.a",
                "29", "android-ndk", "bionic", "libc++"),
        ANDROID_ARMEABI_V7A_JNI(
                "android-armeabi-v7a-jni",
                NativeTarget.android(NativeTarget.Architecture.ARMV7, "armeabi-v7a"),
                NativeBridge.JNI,
                "jParser/runtime/builder/build/c++/libs/android/armeabi-v7a/libruntime.a",
                "jParser/runtime/builder/build/c++/libs/android/armeabi-v7a/libruntime_bridge.a",
                "29", "android-ndk", "bionic", "libc++"),
        ANDROID_ARM64_V8A_JNI(
                "android-arm64-v8a-jni",
                NativeTarget.android(NativeTarget.Architecture.ARM64, "arm64-v8a"),
                NativeBridge.JNI,
                "jParser/runtime/builder/build/c++/libs/android/arm64-v8a/libruntime.a",
                "jParser/runtime/builder/build/c++/libs/android/arm64-v8a/libruntime_bridge.a",
                "29", "android-ndk", "bionic", "libc++"),
        IOS_DEVICE_ARM64_TEAVM_C(
                "ios-device-arm64-teavm-c",
                NativeTarget.ios(NativeTarget.Architecture.ARM64, "device"),
                NativeBridge.TEAVM_C,
                "jParser/runtime/builder/build/c++/libs/ios/device/arm64/teavm_c/"
                        + "libruntime_implementation64_.a",
                "jParser/runtime/builder/build/c++/libs/ios/device/arm64/teavm_c/"
                        + "libruntime_bridge64_.a",
                "14.0", "apple-clang", "libsystem", "libc++"),
        IOS_SIMULATOR_ARM64_TEAVM_C(
                "ios-simulator-arm64-teavm-c",
                NativeTarget.ios(NativeTarget.Architecture.ARM64, "simulator"),
                NativeBridge.TEAVM_C,
                "jParser/runtime/builder/build/c++/libs/ios/simulator/arm64/teavm_c/"
                        + "libruntime_implementation64_.a",
                "jParser/runtime/builder/build/c++/libs/ios/simulator/arm64/teavm_c/"
                        + "libruntime_bridge64_.a",
                "14.0", "apple-clang", "libsystem", "libc++"),
        IOS_SIMULATOR_X86_64_TEAVM_C(
                "ios-simulator-x86_64-teavm-c",
                NativeTarget.ios(NativeTarget.Architecture.X86_64, "simulator"),
                NativeBridge.TEAVM_C,
                "jParser/runtime/builder/build/c++/libs/ios/simulator/x86_64/teavm_c/"
                        + "libruntime_implementation64_.a",
                "jParser/runtime/builder/build/c++/libs/ios/simulator/x86_64/teavm_c/"
                        + "libruntime_bridge64_.a",
                "14.0", "apple-clang", "libsystem", "libc++"),
        WEB(
                "web",
                NativeTarget.web(NativeTarget.Architecture.WASM32),
                NativeBridge.WEB,
                "jParser/runtime/builder/build/c++/libs/emscripten/runtime_.a",
                null,
                "", "emscripten", "emscripten", "libc++");

        private final String id;
        private final NativeTarget target;
        private final NativeBridge bridge;
        private final String implementationPath;
        private final String bridgePath;
        private final String minimumPlatformVersion;
        private final String toolchainId;
        private final String cRuntime;
        private final String cppRuntime;

        RuntimeVariant(
                String id,
                NativeTarget target,
                NativeBridge bridge,
                String implementationPath,
                String bridgePath,
                String minimumPlatformVersion,
                String toolchainId,
                String cRuntime,
                String cppRuntime) {
            this.id = id;
            this.target = target;
            this.bridge = bridge;
            this.implementationPath = implementationPath;
            this.bridgePath = bridgePath;
            this.minimumPlatformVersion = minimumPlatformVersion;
            this.toolchainId = toolchainId;
            this.cRuntime = cRuntime;
            this.cppRuntime = cppRuntime;
        }

        private static RuntimeVariant fromId(String id) {
            for(RuntimeVariant variant : values()) {
                if(variant.id.equals(id)) {
                    return variant;
                }
            }
            throw new IllegalArgumentException("Unknown runtime resources variant: " + id);
        }
    }
}
