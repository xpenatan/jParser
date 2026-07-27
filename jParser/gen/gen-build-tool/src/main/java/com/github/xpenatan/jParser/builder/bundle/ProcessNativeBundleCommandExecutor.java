package com.github.xpenatan.jParser.builder.bundle;

import java.io.IOException;
import java.util.List;

final class ProcessNativeBundleCommandExecutor implements NativeBundleCommandExecutor {
    static final ProcessNativeBundleCommandExecutor INSTANCE = new ProcessNativeBundleCommandExecutor();

    private ProcessNativeBundleCommandExecutor() {
    }

    @Override
    public void execute(NativeBundleCommand command) throws IOException, InterruptedException {
        List<String> arguments = command.getArguments();
        if(arguments.isEmpty()) {
            throw new IllegalArgumentException("Native command must contain an executable");
        }
        ProcessBuilder processBuilder = new ProcessBuilder(arguments);
        processBuilder.directory(command.getWorkingDirectory().toFile());
        processBuilder.environment().putAll(command.getEnvironment());
        processBuilder.redirectErrorStream(true);
        processBuilder.inheritIO();

        Process process;
        try {
            process = processBuilder.start();
        }
        catch(IOException exception) {
            throw new IOException("Unable to start " + command.getDescription() + ": " + arguments.get(0),
                    exception);
        }
        int exitCode = process.waitFor();
        if(exitCode != 0) {
            throw new IOException(command.getDescription() + " failed with exit code " + exitCode);
        }
        for(java.nio.file.Path output : command.getExpectedOutputs()) {
            if(!java.nio.file.Files.isRegularFile(output)) {
                throw new IOException(command.getDescription() + " did not produce expected output: " + output);
            }
        }
    }
}
