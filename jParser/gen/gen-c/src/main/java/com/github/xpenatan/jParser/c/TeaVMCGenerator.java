package com.github.xpenatan.jParser.c;

import com.github.xpenatan.jParser.core.JParser;
import com.github.xpenatan.jParser.ffm.FFMCppGenerator;
import java.io.IOException;
import java.nio.file.Paths;

public class TeaVMCGenerator extends FFMCppGenerator {

    private final String cppDestinationDir;
    private final String libraryName;

    /**
     * Creates the legacy glue-only generator. Use the library-name-aware
     * constructor when portable linkage/loader artifacts are required.
     */
    public TeaVMCGenerator(String cppDestinationDir) {
        super(cppDestinationDir, "teavmcglue", "TeaVMCGlue", "TEAVMC_EXPORT");
        this.cppDestinationDir = cppDestinationDir;
        this.libraryName = null;
    }

    /** Creates glue plus portable linkage/loader artifacts for {@code libraryName}. */
    public TeaVMCGenerator(String cppDestinationDir, String libraryName) {
        super(cppDestinationDir, "teavmcglue", "TeaVMCGlue", "TEAVMC_EXPORT");
        if(libraryName == null || libraryName.trim().isEmpty()) {
            throw new IllegalArgumentException("TeaVM C runtime artifacts require a library name");
        }
        this.cppDestinationDir = cppDestinationDir;
        this.libraryName = libraryName;
    }

    @Override
    public void generate(JParser jParser) {
        super.generate(jParser);
        // Preserve the original one-argument generator contract. Runtime-loaded
        // artifacts need an explicit logical identity and are generated only by
        // the new library-name-aware constructor.
        if(libraryName == null) {
            return;
        }
        try {
            TeaVMCRuntimeArtifacts.generate(Paths.get(cppDestinationDir), libraryName);
        }
        catch(IOException e) {
            throw new RuntimeException("Failed to generate TeaVM C runtime artifacts for " + libraryName, e);
        }
    }
}
