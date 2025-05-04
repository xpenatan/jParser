package com.github.xpenatan.jparser.loader;

import com.badlogic.gdx.utils.SharedLibraryLoader;

public class JParserLibraryLoader {

    private static final SharedLibraryLoader loader = new SharedLibraryLoader();

    private JParserLibraryLoader() {}

    public static void load(JParserLibraryLoaderListener listener, String libraryName) {
        loadInternal(listener, libraryName, "");
    }

    public static void load(JParserLibraryLoaderListener listener, String libraryName, String prefix) {
        loadInternal(listener, libraryName, prefix);
    }

    private static void loadInternal(JParserLibraryLoaderListener listener, String libraryName, String prefix) {
        if(listener == null) {
            throw new RuntimeException("Should implement listener");
        }
        String libName = prefix + libraryName;

        new Thread(() -> {
            try {
                loader.load(libName);
                listener.onLoad(true, null);
            }
            catch(Exception e) {
                listener.onLoad(false, e);
            }
        }).start();
    }
}
