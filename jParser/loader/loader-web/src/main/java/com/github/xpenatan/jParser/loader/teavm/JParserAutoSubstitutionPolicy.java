package com.github.xpenatan.jParser.loader.teavm;

import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.teavm.extension.spi.substitution.SubstitutionPolicy;
import org.teavm.extension.spi.substitution.SubstitutionSink;

public class JParserAutoSubstitutionPolicy implements SubstitutionPolicy {
    private static final String GENERATED_PACKAGE_PREFIX = "gen.web";
    private static final String WEB_CLASS_PREFIX = "Web_";

    @Override
    public void contribute(SubstitutionSink sink) {
        ClassLoader classLoader = getClassLoader();
        Map<String, Boolean> packagePrefixCache = new ConcurrentHashMap<>();
        Map<String, Boolean> classPrefixCache = new ConcurrentHashMap<>();

        sink.selectClasses(className -> hasGeneratedPackageSubstitute(classLoader, packagePrefixCache, className))
                .packagePrefix(GENERATED_PACKAGE_PREFIX);
        sink.selectClasses(className -> hasWebClassPrefixSubstitute(classLoader, classPrefixCache, className))
                .simpleNamePrefix(WEB_CLASS_PREFIX);
    }

    private static boolean hasGeneratedPackageSubstitute(ClassLoader classLoader, Map<String, Boolean> cache, String className) {
        return cache.computeIfAbsent(className, key -> {
            if(shouldSkip(key)) {
                return false;
            }
            String candidate = GENERATED_PACKAGE_PREFIX + "." + key;
            return hasClassResource(classLoader, candidate);
        });
    }

    private static boolean hasWebClassPrefixSubstitute(ClassLoader classLoader, Map<String, Boolean> cache, String className) {
        return cache.computeIfAbsent(className, key -> {
            if(shouldSkip(key)) {
                return false;
            }
            int lastDot = key.lastIndexOf('.');
            String packageName = lastDot >= 0 ? key.substring(0, lastDot + 1) : "";
            String simpleName = lastDot >= 0 ? key.substring(lastDot + 1) : key;
            if(simpleName.startsWith(WEB_CLASS_PREFIX)) {
                return false;
            }
            return hasClassResource(classLoader, packageName + WEB_CLASS_PREFIX + simpleName);
        });
    }

    private static boolean shouldSkip(String className) {
        return className.startsWith("gen.") || className.startsWith("emu.");
    }

    private static boolean hasClassResource(ClassLoader classLoader, String className) {
        String resourceName = className.replace('.', '/') + ".class";
        if(hasResource(classLoader, resourceName)) {
            return true;
        }
        ClassLoader ownClassLoader = JParserAutoSubstitutionPolicy.class.getClassLoader();
        return ownClassLoader != classLoader && hasResource(ownClassLoader, resourceName);
    }

    private static boolean hasResource(ClassLoader classLoader, String resourceName) {
        if(classLoader == null) {
            URL resource = ClassLoader.getSystemResource(resourceName);
            return resource != null;
        }
        return classLoader.getResource(resourceName) != null;
    }

    private static ClassLoader getClassLoader() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if(classLoader != null) {
            return classLoader;
        }
        return JParserAutoSubstitutionPolicy.class.getClassLoader();
    }
}
