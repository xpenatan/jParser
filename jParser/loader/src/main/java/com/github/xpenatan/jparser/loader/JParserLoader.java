package com.github.xpenatan.jparser.loader;

import com.badlogic.gdx.utils.SharedLibraryLoader;
import java.io.File;

public class JParserLoader {

    private SharedLibraryLoader loader = new SharedLibraryLoader();

    public void load(String libraryName) {
        String libCPP = "imgui-cpp";
        String libCore = "imgui-core";
        if (SharedLibraryLoader.isWindows) {
            loader.load(libCPP);
            loader.load(libCore);
        } else {
            if (SharedLibraryLoader.isIos) {
                return;
            }
            if (!SharedLibraryLoader.isLoaded(libCore)) {
                String coreName = loader.mapLibraryName(libCore);
                String cppName = loader.mapLibraryName(libCPP);
                try {
                    String libPath = loader.extractFile(cppName, "imgui-lib").getParentFile().getAbsolutePath();
                    loader.extractFile(coreName, "imgui-lib");
                    String fullPath = libPath + File.separator + coreName;
                    System.load(fullPath);
                    SharedLibraryLoader.setLoaded(libCore);
                } catch (Throwable ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

}
