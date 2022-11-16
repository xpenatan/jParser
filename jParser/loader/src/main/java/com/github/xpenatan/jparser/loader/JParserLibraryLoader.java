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

    public void load(String libraryName, String dependencyLibraryName) {
        if(dependencyLibraryName != null) {
            if(SharedLibraryLoader.isLinux) {
                String dirName = "jparser-lib";
                if(!SharedLibraryLoader.isLoaded(libraryName)) {
                    String lib01Map = loader.mapLibraryName(libraryName);
                    String lib02Map = loader.mapLibraryName(dependencyLibraryName);
                    try {
                        String lib01Path = loader.extractFile(lib01Map, dirName).getParentFile().getAbsolutePath();
                        loader.extractFile(lib02Map, dirName);
                        String fullPath = lib01Path + File.separator + lib01Map;
                        System.load(fullPath);
                        SharedLibraryLoader.setLoaded(libraryName);
                    }
                    catch(Throwable ex) {
                        ex.printStackTrace();
                    }
                }
            }
            else {
                loader.load(dependencyLibraryName);
                loader.load(libraryName);
            }
        }
        else {
            loader.load(libraryName);
        }
    }
}
