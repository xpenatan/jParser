package com.github.xpenatan.jParser.runtime.web.teavm;

import org.teavm.extension.spi.substitution.SubstitutionPolicy;
import org.teavm.extension.spi.substitution.SubstitutionSink;

public class JParserRuntimeWebSubstitutionPolicy implements SubstitutionPolicy {
    private static final String GENERATED_WEB_PACKAGE = "gen.web.";
    private static final String EMULATED_WEB_PACKAGE = "emu.web.";
    private static final String CLASS_SUFFIX = ".class";

    @Override
    public void contribute(SubstitutionSink sink) {
        sink.selectClasses(JParserRuntimeWebSubstitutionPolicy::hasEmulatedWebSubstitute)
                .packagePrefix("emu.web");
        sink.selectClasses(JParserRuntimeWebSubstitutionPolicy::hasGeneratedWebSubstitute)
                .packagePrefix("gen.web");
    }

    private static boolean hasEmulatedWebSubstitute(String className) {
        return hasWebSubstitute(EMULATED_WEB_PACKAGE, className);
    }

    private static boolean hasGeneratedWebSubstitute(String className) {
        return hasWebSubstitute(GENERATED_WEB_PACKAGE, className);
    }

    private static boolean hasWebSubstitute(String packagePrefix, String className) {
        if(className.startsWith(GENERATED_WEB_PACKAGE) || className.startsWith(EMULATED_WEB_PACKAGE)) {
            return false;
        }
        String resourcePath = packagePrefix.replace('.', '/') + className.replace('.', '/') + CLASS_SUFFIX;
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        if(contextClassLoader != null && contextClassLoader.getResource(resourcePath) != null) {
            return true;
        }
        ClassLoader policyClassLoader = JParserRuntimeWebSubstitutionPolicy.class.getClassLoader();
        return policyClassLoader != null && policyClassLoader.getResource(resourcePath) != null;
    }
}
