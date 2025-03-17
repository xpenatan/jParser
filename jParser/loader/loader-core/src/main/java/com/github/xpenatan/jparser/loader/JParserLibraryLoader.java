package com.github.xpenatan.jparser.loader;

import com.badlogic.gdx.utils.SharedLibraryLoader;

public class JParserLibraryLoader {

    private static final SharedLibraryLoader loader = new SharedLibraryLoader();

    private JParserLibraryLoader() {}

    public static void load(String libraryName, JParserLibraryLoaderListener listener) {
        loadInternal(libraryName, listener);
    }

    private static void loadInternal(String libraryName, JParserLibraryLoaderListener listener) {
        if(listener == null) {
            throw new RuntimeException("Should implement listener");
        }
        new Thread(() -> {
            try {
                loader.load(libraryName);
                listener.onLoad(true, null);
            }
            catch(Exception e) {
                e.printStackTrace();
                listener.onLoad(false, e);
            }
        }).start();
    }
}
