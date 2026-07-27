package com.github.xpenatan.jParser.builder.bundle;

import java.nio.file.Path;

public final class NativeComponentArtifact {
    private final Path path;
    private final String classifier;
    private final NativeComponentManifest manifest;

    NativeComponentArtifact(Path path, String classifier, NativeComponentManifest manifest) {
        this.path = path;
        this.classifier = classifier;
        this.manifest = manifest;
    }

    public Path getPath() {
        return path;
    }

    public String getClassifier() {
        return classifier;
    }

    public NativeComponentManifest getManifest() {
        return manifest;
    }
}
