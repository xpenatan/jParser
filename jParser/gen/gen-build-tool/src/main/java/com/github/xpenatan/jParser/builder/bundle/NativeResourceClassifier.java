package com.github.xpenatan.jParser.builder.bundle;

public final class NativeResourceClassifier {
    private NativeResourceClassifier() {
    }

    public static String of(NativeTarget target, NativeBridge bridge, String backendVariant) {
        if(target == null || bridge == null) {
            throw new IllegalArgumentException("Native resource target and bridge are required");
        }
        String classifier = target.getClassifierPrefix();
        if(target.getOperatingSystem() != NativeTarget.OperatingSystem.WEB) {
            classifier += "-" + bridge.getId();
        }
        String backend = normalizeBackend(backendVariant);
        if(!backend.isEmpty()) {
            classifier += "-" + backend;
        }
        return classifier;
    }

    public static String normalizeBackend(String backendVariant) {
        if(backendVariant == null || backendVariant.trim().isEmpty()) {
            return "";
        }
        return NativeTarget.requireSegment("backendVariant", backendVariant);
    }
}
