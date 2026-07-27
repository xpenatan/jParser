package com.github.xpenatan.jParser.builder.bundle;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class NativeBundleCommand {
    private final String description;
    private final Path workingDirectory;
    private final List<String> arguments;
    private final Map<String, String> environment;
    private final List<Path> expectedOutputs;

    NativeBundleCommand(
            String description,
            Path workingDirectory,
            List<String> arguments,
            Map<String, String> environment,
            List<Path> expectedOutputs) {
        this.description = description;
        this.workingDirectory = workingDirectory;
        this.arguments = Collections.unmodifiableList(new ArrayList<>(arguments));
        this.environment = Collections.unmodifiableMap(new LinkedHashMap<>(environment));
        this.expectedOutputs = Collections.unmodifiableList(new ArrayList<>(expectedOutputs));
    }

    String getDescription() {
        return description;
    }

    Path getWorkingDirectory() {
        return workingDirectory;
    }

    List<String> getArguments() {
        return arguments;
    }

    Map<String, String> getEnvironment() {
        return environment;
    }

    List<Path> getExpectedOutputs() {
        return expectedOutputs;
    }
}
