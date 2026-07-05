package com.github.xpenatan.jparser.runtime.c.teavm;

import org.teavm.extension.spi.substitution.SubstitutionPolicy;
import org.teavm.extension.spi.substitution.SubstitutionSink;

public class JParserRuntimeCSubstitutionPolicy implements SubstitutionPolicy {
    private static final String GENERATED_C_PACKAGE = "gen.c.";
    private static final String EMULATED_C_PACKAGE = "emu.c.";
    private static final String CLASS_SUFFIX = ".class";

    @Override
    public void contribute(SubstitutionSink sink) {
        sink.selectClasses(JParserRuntimeCSubstitutionPolicy::hasEmulatedCSubstitute)
                .packagePrefix("emu.c");
        sink.selectClasses(JParserRuntimeCSubstitutionPolicy::hasGeneratedCSubstitute)
                .packagePrefix("gen.c");
    }

    private static boolean hasEmulatedCSubstitute(String className) {
        return hasCSubstitute(EMULATED_C_PACKAGE, className);
    }

    private static boolean hasGeneratedCSubstitute(String className) {
        return hasCSubstitute(GENERATED_C_PACKAGE, className);
    }

    private static boolean hasCSubstitute(String packagePrefix, String className) {
        if(className.startsWith(GENERATED_C_PACKAGE) || className.startsWith(EMULATED_C_PACKAGE)) {
            return false;
        }
        String resourcePath = packagePrefix.replace('.', '/') + className.replace('.', '/') + CLASS_SUFFIX;
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        if(contextClassLoader != null && contextClassLoader.getResource(resourcePath) != null) {
            return true;
        }
        ClassLoader policyClassLoader = JParserRuntimeCSubstitutionPolicy.class.getClassLoader();
        return policyClassLoader != null && policyClassLoader.getResource(resourcePath) != null;
    }
}
