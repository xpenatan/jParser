package com.github.xpenatan.jParser.builder.bundle;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class NativeBundleOutputPaths {
    private NativeBundleOutputPaths() {
    }

    public static List<Path> forTarget(
            String bundleName,
            NativeTarget target,
            NativeWebOutput webOutput,
            Path outputDirectory) {
        if(bundleName == null || bundleName.trim().isEmpty()) {
            throw new IllegalArgumentException("bundleName must not be blank");
        }
        if(target == null || outputDirectory == null) {
            throw new IllegalArgumentException("Native bundle target and output directory are required");
        }
        ArrayList<Path> outputs = new ArrayList<>();
        switch(target.getOperatingSystem()) {
            case WINDOWS:
            case LINUX:
            case MACOS:
                outputs.add(outputDirectory.resolve(sharedLibraryName(bundleName.trim(), target)));
                break;
            case ANDROID:
                outputs.add(outputDirectory.resolve("lib" + bundleName.trim() + ".so"));
                break;
            case IOS:
                outputs.add(outputDirectory.resolve("lib" + bundleName.trim() + ".a"));
                break;
            case WEB:
                if(webOutput == null) {
                    throw new IllegalArgumentException("webOutput is required for a web target");
                }
                outputs.add(outputDirectory.resolve(bundleName.trim() + ".js"));
                if(webOutput == NativeWebOutput.JAVASCRIPT_AND_WASM) {
                    outputs.add(outputDirectory.resolve(bundleName.trim() + ".wasm"));
                }
                break;
            default:
                throw new IllegalArgumentException("Unsupported native bundle target: " + target);
        }
        return Collections.unmodifiableList(outputs);
    }

    static String sharedLibraryName(String bundleName, NativeTarget target) {
        String architectureSuffix;
        switch(target.getArchitecture()) {
            case X86:
                architectureSuffix = "";
                break;
            case X86_64:
                architectureSuffix = "64";
                break;
            case ARMV7:
                architectureSuffix = "arm";
                break;
            case ARM64:
                architectureSuffix = "arm64";
                break;
            default:
                throw new IllegalArgumentException("Unsupported shared-library architecture: "
                        + target.getArchitecture().getId());
        }
        switch(target.getOperatingSystem()) {
            case WINDOWS:
                return bundleName + architectureSuffix + ".dll";
            case LINUX:
                return "lib" + bundleName + architectureSuffix + ".so";
            case MACOS:
                return "lib" + bundleName + architectureSuffix + ".dylib";
            default:
                throw new IllegalArgumentException("Target does not produce a desktop shared library: " + target);
        }
    }
}
