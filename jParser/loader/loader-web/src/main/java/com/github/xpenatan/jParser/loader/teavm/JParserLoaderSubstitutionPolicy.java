package com.github.xpenatan.jParser.loader.teavm;

import org.teavm.extension.spi.substitution.SubstitutionPolicy;
import org.teavm.extension.spi.substitution.SubstitutionSink;

public class JParserLoaderSubstitutionPolicy implements SubstitutionPolicy {
    @Override
    public void contribute(SubstitutionSink sink) {
        sink.substitutePackage("com.github.xpenatan.jParser.loader", "emu.com.github.xpenatan.jParser.loader");
    }
}
