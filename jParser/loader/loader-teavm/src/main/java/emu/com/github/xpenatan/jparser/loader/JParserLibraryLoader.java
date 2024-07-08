package emu.com.github.xpenatan.jparser.loader;

import com.github.xpenatan.gdx.backends.teavm.assetloader.AssetLoader;
import com.github.xpenatan.gdx.backends.teavm.assetloader.AssetLoaderListener;
import java.util.HashSet;

public class JParserLibraryLoader {

    private static HashSet<String> loadedLibraries = new HashSet<>();

    public JParserLibraryLoader() {
    }

    public void load(String libraryName) {
        loadInternal(libraryName, null);
    }

    public void load(String libraryName, Runnable runnable) {
        loadInternal(libraryName, runnable);
    }

    public void loadInternal(String libraryName, Runnable runnable) {
        if(loadedLibraries.contains(libraryName)) {
            return;
        }
        loadedLibraries.add(libraryName);
        AssetLoader.AssetLoad instance = AssetLoader.getInstance();
        instance.loadScript(true, libraryName, new AssetLoaderListener<>(){
            @Override
            public void onSuccess(String url, String result) {
                if(runnable != null) {
                    runnable.run();
                }
            }
        });
    }
}