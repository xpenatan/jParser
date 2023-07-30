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

    @Deprecated
    public void load(String libraryName) {
        load(libraryName, null, null);
    }

    @Deprecated
    public void load(String libraryName01, String libraryName02) {
        load(libraryName01, libraryName02, null);
    }

    public void load(String libraryName, Runnable runnable) {
        load(libraryName, null, runnable);
    }

    public void load(String libraryName01, String libraryName02, Runnable runnable) {
        if(libraryName02 != null) {
            if(SharedLibraryLoader.isLinux || SharedLibraryLoader.isMac || SharedLibraryLoader.isWindows) {
                String dirName = "jparser-lib";
                boolean isLib01Loaded = SharedLibraryLoader.isLoaded(libraryName01);
                boolean isLib02Loaded = SharedLibraryLoader.isLoaded(libraryName02);
                if(!isLib01Loaded) {
                    String lib01Map = loader.mapLibraryName(libraryName01);
                    String lib02Map = loader.mapLibraryName(libraryName02);
                    try {
                        String lib01Path = loader.extractFile(lib01Map, dirName).getParentFile().getAbsolutePath();
                        String lib02Path = loader.extractFile(lib02Map, dirName).getParentFile().getAbsolutePath();

                        String fullPath01 = lib01Path + File.separator + lib01Map;
                        String fullPath02 = lib02Path + File.separator + lib02Map;

                        if(!isLib02Loaded) {
                            System.load(fullPath02);
                            SharedLibraryLoader.setLoaded(libraryName02);
                        }
                        System.load(fullPath01);
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
        runnable.run();
    }
}
