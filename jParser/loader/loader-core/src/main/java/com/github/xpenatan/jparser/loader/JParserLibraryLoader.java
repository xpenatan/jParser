package com.github.xpenatan.jparser.loader;

import com.badlogic.gdx.utils.SharedLibraryLoader;

public class JParserLibraryLoader {

    private static final SharedLibraryLoader loader = new SharedLibraryLoader();

    private JParserLibraryLoader() {}

    public static void load(JParserLibraryLoaderListener listener, String libraryName) {
        loadInternal(listener, libraryName);
    }

    private static void loadInternal(JParserLibraryLoaderListener listener, String libraryName) {
        if(listener == null) {
            throw new RuntimeException("Should implement listener");
        }
        new Thread(() -> {
            try {
                loader.load(libraryName);
                listener.onLoad(true, null);
            }
            catch(Exception e) {
                listener.onLoad(false, e);
            }
        }).start();
    }
}
