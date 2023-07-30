package com.github.xpenatan.jparser.example.lib;

import com.github.xpenatan.jparser.loader.JParserLibraryLoader;

public class ExampleLib {

    public static void init(Runnable run) {
        JParserLibraryLoader libraryLoader = new JParserLibraryLoader();
        libraryLoader.load("exampleLib", run);
    }
}
