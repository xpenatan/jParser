package com.github.xpenatan.jparser.loader;

import com.badlogic.gdx.utils.SharedLibraryLoader;

public class JParserLibraryLoader {

    private final SharedLibraryLoader loader;

    public JParserLibraryLoader() {
        loader = new SharedLibraryLoader();
    }

    public JParserLibraryLoader(String nativesJar) {
        loader = new SharedLibraryLoader(nativesJar);
    }

    public void load(String libraryName) {
        loadInternal(libraryName, null);
    }

    public void load(String libraryName, JParserLibraryLoaderListener listener) {
        loadInternal(libraryName, listener);
    }

    public void loadInternal(String libraryName, JParserLibraryLoaderListener listener) {
        if(listener != null) {
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
        else {
            loader.load(libraryName);
        }
    }
}
