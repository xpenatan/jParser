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

    public void load(String libraryName, Runnable runnable) {
        loadInternal(libraryName, runnable);
    }

    public void loadInternal(String libraryName, Runnable runnable) {
        loader.load(libraryName);
        if(runnable != null) {
            runnable.run();
        }
    }
}
