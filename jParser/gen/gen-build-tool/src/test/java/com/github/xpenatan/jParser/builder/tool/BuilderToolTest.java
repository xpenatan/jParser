package com.github.xpenatan.jParser.builder.tool;

import static org.junit.Assert.assertFalse;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class BuilderToolTest {

    @Rule
    public final TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void teaVMCGenerationRemovesStalePublicPackageOutput() throws Exception {
        Path output = temporaryFolder.newFolder("c-output").toPath();
        Path stalePublicClass = output.resolve("com/example/PublicApi.java");
        Files.createDirectories(stalePublicClass.getParent());
        Files.write(stalePublicClass, "package com.example;".getBytes(StandardCharsets.UTF_8));

        BuilderTool.cleanGeneratedTeaVMCJavaOutput(output.toString());

        assertFalse(Files.exists(output));
    }
}
