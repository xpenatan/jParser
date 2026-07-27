package com.github.xpenatan.jParser.builder.bundle;

import java.io.IOException;

interface NativeBundleCommandExecutor {
    void execute(NativeBundleCommand command) throws IOException, InterruptedException;
}
