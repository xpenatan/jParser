package emu.com.github.xpenatan.jparser.loader;

import com.github.xpenatan.gdx.backends.teavm.assetloader.AssetInstance;
import com.github.xpenatan.gdx.backends.teavm.assetloader.AssetLoader;
import com.github.xpenatan.gdx.backends.teavm.assetloader.AssetLoaderListener;
import com.github.xpenatan.jparser.loader.JParserLibraryLoaderListener;
import java.util.HashSet;
import javax.script.ScriptException;
import org.teavm.jso.JSBody;
import org.teavm.jso.JSFunctor;

public class JParserLibraryLoader {

    private static HashSet<String> loadedLibraries = new HashSet<>();

    private JParserLibraryLoader() {}

    public static void load(String libraryName, JParserLibraryLoaderListener listener) {
        loadInternal(libraryName, listener);
    }

    private static void loadInternal(final String libraryName, JParserLibraryLoaderListener listener) {
        if(listener == null) {
            throw new RuntimeException("Should implement listener");
        }

        if(loadedLibraries.contains(libraryName)) {
            return;
        }

        // Try to load wasm first
        loadWasm(libraryName, listener);
    }

    private static void loadWasm(final String libraryName, JParserLibraryLoaderListener listener) {
        AssetLoader instance = AssetInstance.getLoaderInstance();
        instance.loadScript(libraryName + ".wasm.js", new AssetLoaderListener<>() {
            @Override
            public void onSuccess(String url, String result) {
                // Wasm requires to setup wasm first
                String fullLibName = libraryName + "OnInit";
                setOnLoadInit(fullLibName, () -> {
                    loadedLibraries.add(libraryName);
                    listener.onLoad(true, null);
                });
            }
            @Override
            public void onFailure(String url) {
                // Fallback to javascript
                loadJS(libraryName, listener);
            }
        });
    }

    private static void loadJS(final String libraryName, JParserLibraryLoaderListener listener) {
        AssetLoader instance = AssetInstance.getLoaderInstance();
        instance.loadScript(libraryName + ".js", new AssetLoaderListener<>() {
            @Override
            public void onSuccess(String url, String result) {
                loadedLibraries.add(libraryName);
                listener.onLoad(true, null);
            }
            @Override
            public void onFailure(String url) {
                listener.onLoad(false, new ScriptException("Failed to load script: " + url));
            }
        });
    }

    @JSFunctor
    public interface OnInitFunction extends org.teavm.jso.JSObject {
        void onInit();
    }

    @JSBody(params = { "libraryName", "onInitFunction" }, script = "window[libraryName] = onInitFunction;")
    private static native void setOnLoadInit(String libraryName, OnInitFunction onInitFunction);
}