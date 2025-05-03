package com.github.xpenatan.jparser.loader;

import com.badlogic.gdx.utils.SharedLibraryLoader;

public class JParserLibraryLoader {

    public static String PREFIX = "";

    private static final SharedLibraryLoader loader = new SharedLibraryLoader();

    private JParserLibraryLoader() {}

    public static void load(String libraryName, JParserLibraryLoaderListener listener) {
        loadInternal(libraryName, listener);
    }

    private static void loadInternal(String libraryName, JParserLibraryLoaderListener listener) {
        if(listener == null) {
            throw new RuntimeException("Should implement listener");
        }
        String libName = PREFIX + libraryName;

        new Thread(() -> {
            try {
                loader.load(libName);
                listener.onLoad(true, null);
            }
            catch(Exception e) {
                e.printStackTrace();
                listener.onLoad(false, e);
            }
        }).start();
    }
}
