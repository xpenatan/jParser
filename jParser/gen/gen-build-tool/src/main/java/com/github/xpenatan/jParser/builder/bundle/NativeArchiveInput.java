package com.github.xpenatan.jParser.builder.bundle;

import java.nio.file.Path;
import java.util.Objects;

public final class NativeArchiveInput {
    private final String name;
    private final Path path;
    private final NativeArchiveLinkMode linkMode;

    public NativeArchiveInput(String name, Path path, NativeArchiveLinkMode linkMode) {
        this.name = requireName(name);
        this.path = Objects.requireNonNull(path, "path");
        this.linkMode = Objects.requireNonNull(linkMode, "linkMode");
    }

    public String getName() {
        return name;
    }

    public Path getPath() {
        return path;
    }

    public NativeArchiveLinkMode getLinkMode() {
        return linkMode;
    }

    private static String requireName(String value) {
        if(value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Archive name must not be blank");
        }
        return value.trim();
    }
}
