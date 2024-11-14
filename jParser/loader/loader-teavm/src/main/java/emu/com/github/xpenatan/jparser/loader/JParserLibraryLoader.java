package emu.com.github.xpenatan.jparser.loader;

import com.github.xpenatan.gdx.backends.teavm.assetloader.AssetInstance;
import com.github.xpenatan.gdx.backends.teavm.assetloader.AssetLoader;
import com.github.xpenatan.gdx.backends.teavm.assetloader.AssetLoaderListener;
import com.github.xpenatan.jparser.loader.JParserLibraryLoaderListener;
import java.util.HashSet;

public class JParserLibraryLoader {

    private static HashSet<String> loadedLibraries = new HashSet<>();

    public JParserLibraryLoader() {
    }

    public void load(String libraryName) {
        loadInternal(libraryName, null);
    }

    public void load(String libraryName, JParserLibraryLoaderListener listener) {
        loadInternal(libraryName, listener);
    }

    public void loadInternal(String libraryName, JParserLibraryLoaderListener listener) {
        if(!libraryName.endsWith(".js")) {
            libraryName = libraryName + ".js";
        }

        if(loadedLibraries.contains(libraryName)) {
            return;
        }
        loadedLibraries.add(libraryName);
        AssetLoader instance = AssetInstance.getLoaderInstance();
        if(listener != null) {
            instance.loadScript(libraryName, new AssetLoaderListener<>(){
                @Override
                public void onSuccess(String url, String result) {
                    listener.onLoad(true);
                }
                @Override
                public void onFailure(String url) {
                    listener.onLoad(false);
                }
            });
        }
        else {
            instance.loadScript(libraryName);
        }
    }
}