package com.github.xpenatan.jParser.builder.bundle;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class NativeBundleResult {
    private final List<Path> outputPaths;

    NativeBundleResult(List<Path> outputPaths) {
        this.outputPaths = Collections.unmodifiableList(new ArrayList<>(outputPaths));
    }

    public List<Path> getOutputPaths() {
        return outputPaths;
    }

    public Path getPrimaryOutputPath() {
        if(outputPaths.isEmpty()) {
            throw new IllegalStateException("The native bundle has no output");
        }
        return outputPaths.get(0);
    }
}
