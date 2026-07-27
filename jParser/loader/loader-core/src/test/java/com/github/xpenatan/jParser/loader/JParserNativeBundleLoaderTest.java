package com.github.xpenatan.jParser.loader;

import static org.junit.Assert.assertTrue;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import org.junit.Test;

public class JParserNativeBundleLoaderTest {
    @Test
    public void exposesOneShotLoaderWithoutChangingStandaloneLoader() throws Exception {
        Method defaultLoad = JParserNativeBundleLoader.class.getMethod(
                "load",
                String.class,
                JParserLibraryLoaderListener.class);
        Method configuredLoad = JParserNativeBundleLoader.class.getMethod(
                "load",
                String.class,
                JParserLibraryLoaderOptions.class,
                JParserLibraryLoaderListener.class);
        Constructor<?> constructor = JParserNativeBundleLoader.class.getDeclaredConstructor();

        assertTrue(Modifier.isStatic(defaultLoad.getModifiers()));
        assertTrue(Modifier.isStatic(configuredLoad.getModifiers()));
        assertTrue(Modifier.isPrivate(constructor.getModifiers()));
    }
}
