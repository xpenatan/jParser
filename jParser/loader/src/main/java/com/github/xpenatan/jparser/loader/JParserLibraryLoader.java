package com.github.xpenatan.jparser.loader;

import com.badlogic.gdx.utils.SharedLibraryLoader;
import java.io.File;

public class JParserLibraryLoader {

    private final SharedLibraryLoader loader;

    public JParserLibraryLoader() {
        loader = new SharedLibraryLoader();
    }

    public JParserLibraryLoader(String nativesJar) {
        loader = new SharedLibraryLoader(nativesJar);
    }

    public void load(String libraryName) {
        load(libraryName, null);
    }

    public void load(String libraryName01, String libraryName02) {
        if(libraryName02 != null) {
            if(SharedLibraryLoader.isLinux || SharedLibraryLoader.isMac) {
                String dirName = "jparser-lib";
                if(!SharedLibraryLoader.isLoaded(libraryName01)) {
                    String lib01Map = loader.mapLibraryName(libraryName01);
                    String lib02Map = loader.mapLibraryName(libraryName02);
                    try {
                        String lib01Path = loader.extractFile(lib01Map, dirName).getParentFile().getAbsolutePath();
                        loader.extractFile(lib02Map, dirName);
                        String fullPath = lib01Path + File.separator + lib01Map;
                        System.load(fullPath);
                        SharedLibraryLoader.setLoaded(libraryName01);
                    }
                    catch(Throwable ex) {
                        ex.printStackTrace();
                    }
                }
            }
            else {
                loader.load(libraryName02);
                loader.load(libraryName01);
            }
        }
        else {
            loader.load(libraryName01);
        }
    }
}
