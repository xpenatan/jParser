package com.github.xpenatan.jParser.builder.bundle;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;

public final class NativeBundleRequest {
    public String bundleName;
    public Path outputDirectory;
    public NativeTarget target;
    public NativeBuildType buildType = NativeBuildType.RELEASE;
    public final ArrayList<Path> componentJars = new ArrayList<>();

    public NativeWebOutput webOutput = NativeWebOutput.JAVASCRIPT_AND_WASM;
    public int androidApiLevel = 28;
    public String minimumMacOSVersion = "10.13";
    public String minimumIOSVersion = "12.0";

    /**
     * Optional linker/compiler command override. This is useful for custom or cross toolchains.
     */
    public String linkerExecutable = "";
    public String visualCppEnvironment = "";
    public String androidNdkHome = "";
    public String emscriptenRoot = "";
    public String pythonExecutable = "";
    public final LinkedHashMap<String, String> environment = new LinkedHashMap<>();

    /**
     * Retains the generated merge/extraction directory for diagnostics.
     */
    public boolean keepTemporaryFiles;
}
