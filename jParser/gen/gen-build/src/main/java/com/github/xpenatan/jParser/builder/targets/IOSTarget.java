package com.github.xpenatan.jParser.builder.targets;

import com.github.xpenatan.jParser.builder.BuildConfig;
import com.github.xpenatan.jParser.builder.DefaultBuildTarget;

/** Builds a native library slice with the Apple iOS toolchain. */
public class IOSTarget extends DefaultBuildTarget {

    public static final String MIN_IOS_VERSION = "14.0";

    public enum SDK {
        DEVICE("iphoneos", "device", "-miphoneos-version-min="),
        SIMULATOR("iphonesimulator", "simulator", "-mios-simulator-version-min=");

        private final String xcrunName;
        private final String resourceName;
        private final String deploymentFlag;

        SDK(String xcrunName, String resourceName, String deploymentFlag) {
            this.xcrunName = xcrunName;
            this.resourceName = resourceName;
            this.deploymentFlag = deploymentFlag;
        }

        public String getXcrunName() {
            return xcrunName;
        }

        public String getResourceName() {
            return resourceName;
        }
    }

    public enum Architecture {
        ARM64("arm64"),
        X86_64("x86_64");

        private final String clangName;

        Architecture(String clangName) {
            this.clangName = clangName;
        }

        public String getClangName() {
            return clangName;
        }
    }

    private final SDK sdk;
    private final Architecture architecture;
    private final String minIOSVersion;

    public IOSTarget() {
        this(SourceLanguage.CPP, SDK.SIMULATOR, Architecture.X86_64, MIN_IOS_VERSION);
    }

    public IOSTarget(SourceLanguage language) {
        this(language, SDK.SIMULATOR, Architecture.X86_64, MIN_IOS_VERSION);
    }

    public IOSTarget(SourceLanguage language, SDK sdk, Architecture architecture) {
        this(language, sdk, architecture, MIN_IOS_VERSION);
    }

    public static IOSTarget[] createStaticLibrarySlices(SourceLanguage language) {
        IOSTarget[] targets = new IOSTarget[] {
                new IOSTarget(language, SDK.DEVICE, Architecture.ARM64),
                new IOSTarget(language, SDK.SIMULATOR, Architecture.ARM64),
                new IOSTarget(language, SDK.SIMULATOR, Architecture.X86_64)
        };
        for(IOSTarget target : targets) {
            target.isStatic = true;
        }
        return targets;
    }

    public IOSTarget(SourceLanguage language, SDK sdk, Architecture architecture, String minIOSVersion) {
        if(sdk == null) {
            throw new IllegalArgumentException("iOS SDK must not be null");
        }
        if(architecture == null) {
            throw new IllegalArgumentException("iOS architecture must not be null");
        }
        if(sdk == SDK.DEVICE && architecture != Architecture.ARM64) {
            throw new IllegalArgumentException("iOS device libraries currently support ARM64 only");
        }
        if(minIOSVersion == null || minIOSVersion.trim().isEmpty()) {
            throw new IllegalArgumentException("Minimum iOS version must not be blank");
        }

        this.sdk = sdk;
        this.architecture = architecture;
        this.minIOSVersion = minIOSVersion.trim();
        this.libDirSuffix = getResourcePlatform() + "/";
        this.tempBuildDir = "target/" + getResourcePlatform() + "/";
        this.libPrefix = "lib";

        String compiler = language == SourceLanguage.C ? "clang" : "clang++";
        addXcrunTool(cppCompiler, compiler);
        addXcrunTool(linkerCompiler, compiler);

        cppFlags.add("-arch");
        cppFlags.add(architecture.clangName);
        cppFlags.add(sdk.deploymentFlag + this.minIOSVersion);
        cppFlags.add("-fPIC");
        cppFlags.add("-c");
        cppFlags.add("-Wall");
        cppFlags.add("-O2");
        cppFlags.add("-fmessage-length=0");
        cppFlags.add("-Wno-unused-variable");
        cppFlags.add("-Wno-unused-but-set-variable");
        cppFlags.add("-Wno-format");
        if(language == SourceLanguage.CPP) {
            cppFlags.add("-stdlib=libc++");
        }
    }

    public SDK getSdk() {
        return sdk;
    }

    public Architecture getArchitecture() {
        return architecture;
    }

    public String getMinIOSVersion() {
        return minIOSVersion;
    }

    public String getResourcePlatform() {
        return "ios/" + sdk.resourceName + "/" + architecture.clangName;
    }

    @Override
    protected void setup(BuildConfig config) {
        if(isStatic) {
            linkerCompiler.clear();
            addXcrunTool(linkerCompiler, "libtool");
            linkerFlags.add("-static");
            linkerFlags.add("-o");
            libSuffix = "64_.a";
            linkerOutputCommand = "";
            return;
        }

        linkerFlags.add("-arch");
        linkerFlags.add(architecture.clangName);
        linkerFlags.add(sdk.deploymentFlag + minIOSVersion);
        linkerFlags.add("-dynamiclib");
        linkerFlags.add("-stdlib=libc++");
        libSuffix = "64.dylib";
        linkerOutputCommand = "-o";
    }

    private void addXcrunTool(java.util.ArrayList<String> command, String tool) {
        command.add("xcrun");
        command.add("--sdk");
        command.add(sdk.xcrunName);
        command.add(tool);
    }
}
