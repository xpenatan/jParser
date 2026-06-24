package com.github.xpenatan.jParser.c;

import com.github.xpenatan.jParser.ffm.FFMCppGenerator;

public class TeaVMCGenerator extends FFMCppGenerator {

    public TeaVMCGenerator(String cppDestinationDir) {
        super(cppDestinationDir, "teavmcglue", "TeaVMCGlue", "TEAVMC_EXPORT");
    }
}
