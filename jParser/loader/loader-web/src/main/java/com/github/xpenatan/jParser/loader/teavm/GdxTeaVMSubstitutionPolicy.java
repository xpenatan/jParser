package com.github.xpenatan.jParser.loader.teavm;

import org.teavm.extension.spi.substitution.SubstitutionPolicy;
import org.teavm.extension.spi.substitution.SubstitutionSink;

public class GdxTeaVMSubstitutionPolicy implements SubstitutionPolicy {
    @Override
    public void contribute(SubstitutionSink sink) {
        sink.substitutePackage("java", "emu.java");
        sink.substitutePackage("com", "emu.com");
        sink.substitutePackage("org", "emu.org");
        sink.substitutePackage("net", "emu.net");
    }
}
