package com.github.xpenatan.jParser.builder.bundle;

import java.util.Locale;
import java.util.Objects;

public final class NativeTarget {
    public enum OperatingSystem {
        WINDOWS("windows"),
        LINUX("linux"),
        MACOS("macos"),
        ANDROID("android"),
        IOS("ios"),
        WEB("web");

        private final String id;

        OperatingSystem(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }

        static OperatingSystem fromId(String id) {
            for(OperatingSystem value : values()) {
                if(value.id.equals(id)) {
                    return value;
                }
            }
            throw new IllegalArgumentException("Unsupported native operating system: " + id);
        }
    }

    public enum Architecture {
        X86("x86"),
        X86_64("x86_64"),
        ARMV7("armv7"),
        ARM64("arm64"),
        WASM32("wasm32"),
        WASM64("wasm64");

        private final String id;

        Architecture(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }

        static Architecture fromId(String id) {
            for(Architecture value : values()) {
                if(value.id.equals(id)) {
                    return value;
                }
            }
            throw new IllegalArgumentException("Unsupported native architecture: " + id);
        }
    }

    private final OperatingSystem operatingSystem;
    private final Architecture architecture;
    private final String abi;
    private final String environment;

    private NativeTarget(OperatingSystem operatingSystem, Architecture architecture, String abi, String environment) {
        this.operatingSystem = Objects.requireNonNull(operatingSystem, "operatingSystem");
        this.architecture = Objects.requireNonNull(architecture, "architecture");
        this.abi = normalizeOptional("abi", abi);
        this.environment = normalizeOptional("environment", environment);
        validateCombination();
    }

    public static NativeTarget of(OperatingSystem operatingSystem, Architecture architecture) {
        return new NativeTarget(operatingSystem, architecture, "", "");
    }

    /**
     * Returns the desktop target of the JVM running the builder.
     *
     * <p>This is a convenience for host-native development builds. Release
     * matrices and cross-compilation should continue to declare their target
     * explicitly.</p>
     */
    public static NativeTarget currentDesktop() {
        return currentDesktop(
                System.getProperty("os.name", ""),
                System.getProperty("os.arch", ""));
    }

    public static NativeTarget android(Architecture architecture, String abi) {
        return new NativeTarget(OperatingSystem.ANDROID, architecture, requireSegment("abi", abi), "");
    }

    public static NativeTarget ios(Architecture architecture, String environment) {
        return new NativeTarget(OperatingSystem.IOS, architecture, "", requireSegment("environment", environment));
    }

    public static NativeTarget web(Architecture architecture) {
        return new NativeTarget(OperatingSystem.WEB, architecture, "", "");
    }

    static NativeTarget fromManifest(String os, String architecture, String abi, String environment) {
        return new NativeTarget(OperatingSystem.fromId(os), Architecture.fromId(architecture), abi, environment);
    }

    static NativeTarget currentDesktop(String osName, String architectureName) {
        String normalizedOS = osName.toLowerCase(Locale.ROOT);
        OperatingSystem operatingSystem;
        if(normalizedOS.contains("mac") || normalizedOS.contains("darwin")) {
            operatingSystem = OperatingSystem.MACOS;
        }
        else if(normalizedOS.contains("win")) {
            operatingSystem = OperatingSystem.WINDOWS;
        }
        else if(normalizedOS.contains("linux")) {
            operatingSystem = OperatingSystem.LINUX;
        }
        else {
            throw new IllegalStateException("Unsupported desktop operating system: " + osName);
        }

        String normalizedArchitecture = architectureName
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]", "");
        Architecture architecture;
        if(normalizedArchitecture.equals("amd64")
                || normalizedArchitecture.equals("x8664")
                || normalizedArchitecture.equals("x64")) {
            architecture = Architecture.X86_64;
        }
        else if(normalizedArchitecture.equals("x86")
                || normalizedArchitecture.matches("i[3-6]86")) {
            architecture = Architecture.X86;
        }
        else if(normalizedArchitecture.equals("aarch64")
                || normalizedArchitecture.equals("arm64")) {
            architecture = Architecture.ARM64;
        }
        else if(normalizedArchitecture.startsWith("armv7")
                || normalizedArchitecture.equals("arm")) {
            architecture = Architecture.ARMV7;
        }
        else {
            throw new IllegalStateException("Unsupported desktop architecture: " + architectureName);
        }
        return of(operatingSystem, architecture);
    }

    public OperatingSystem getOperatingSystem() {
        return operatingSystem;
    }

    public Architecture getArchitecture() {
        return architecture;
    }

    public String getAbi() {
        return abi;
    }

    public String getEnvironment() {
        return environment;
    }

    public String getClassifierPrefix() {
        if(operatingSystem == OperatingSystem.WEB) {
            return "web";
        }
        if(operatingSystem == OperatingSystem.ANDROID) {
            return "android-" + abi;
        }
        if(operatingSystem == OperatingSystem.IOS) {
            return "ios-" + environment + "-" + architecture.id;
        }
        return operatingSystem.id + "-" + architecture.id;
    }

    private void validateCombination() {
        if(operatingSystem == OperatingSystem.WEB) {
            if(architecture != Architecture.WASM32 && architecture != Architecture.WASM64) {
                throw new IllegalArgumentException("Web target requires wasm32 or wasm64 architecture");
            }
        }
        else if(architecture == Architecture.WASM32 || architecture == Architecture.WASM64) {
            throw new IllegalArgumentException("Wasm architecture is only valid for a web target");
        }
        if(operatingSystem == OperatingSystem.ANDROID && abi.isEmpty()) {
            throw new IllegalArgumentException("Android target requires an ABI");
        }
        if(operatingSystem != OperatingSystem.ANDROID && !abi.isEmpty()) {
            throw new IllegalArgumentException("ABI is only valid for an Android target");
        }
        if(operatingSystem == OperatingSystem.IOS && environment.isEmpty()) {
            throw new IllegalArgumentException("iOS target requires an environment");
        }
        if(operatingSystem != OperatingSystem.IOS && !environment.isEmpty()) {
            throw new IllegalArgumentException("Environment is only valid for an iOS target");
        }
    }

    private static String normalizeOptional(String name, String value) {
        if(value == null || value.trim().isEmpty()) {
            return "";
        }
        return requireSegment(name, value);
    }

    static String requireSegment(String name, String value) {
        if(value == null) {
            throw new IllegalArgumentException(name + " must not be null");
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        if(!normalized.matches("[a-z0-9][a-z0-9._-]*")) {
            throw new IllegalArgumentException(name + " contains unsupported characters: " + value);
        }
        return normalized;
    }

    @Override
    public boolean equals(Object object) {
        if(this == object) {
            return true;
        }
        if(!(object instanceof NativeTarget)) {
            return false;
        }
        NativeTarget other = (NativeTarget)object;
        return operatingSystem == other.operatingSystem
                && architecture == other.architecture
                && abi.equals(other.abi)
                && environment.equals(other.environment);
    }

    @Override
    public int hashCode() {
        return Objects.hash(operatingSystem, architecture, abi, environment);
    }

    @Override
    public String toString() {
        return getClassifierPrefix();
    }
}
