package emu.com.github.xpenatan.jparser.loader;

import com.github.xpenatan.gdx.backends.teavm.assetloader.AssetInstance;
import com.github.xpenatan.gdx.backends.teavm.assetloader.AssetLoader;
import com.github.xpenatan.jparser.loader.JParserLibraryLoaderListener;
import java.util.HashSet;
import javax.script.ScriptException;
import org.teavm.jso.JSBody;
import org.teavm.jso.JSFunctor;
import org.teavm.jso.browser.Window;
import org.teavm.jso.dom.html.HTMLDocument;
import org.teavm.jso.dom.html.HTMLScriptElement;

public class JParserLibraryLoader {

    private static HashSet<String> loadedLibraries = new HashSet<>();

    private JParserLibraryLoader() {}

    public static void load(JParserLibraryLoaderListener listener, String libraryName) {
        loadInternal(listener, libraryName, "");
    }

    public static void load(JParserLibraryLoaderListener listener, String libraryName, String prefix) {
        loadInternal(listener, libraryName, prefix);
    }

    private static void loadInternal(JParserLibraryLoaderListener listener, String libraryName, String prefix) {
        if(listener == null) {
            throw new RuntimeException("Should implement listener");
        }

        if(loadedLibraries.contains(libraryName)) {
            return;
        }

        JParserLibraryLoaderListener lis = new JParserLibraryLoaderListener() {
            @Override
            public void onLoad(boolean isSuccess, Exception e) {
                listener.onLoad(isSuccess, e);
            }
        };

        if(libraryName.endsWith(".wasm.js")) {
            loadWasm(lis, libraryName, prefix, "", false);
        }
        else if(libraryName.endsWith(".js")) {
            loadJS(lis, libraryName, prefix);
        }
        else {
            AssetLoader instance = AssetInstance.getLoaderInstance();
            if(instance != null) {
                // If gdx-teavm is used obtain the script path;
                String scriptUrl = instance.getScriptUrl();
                loadWasm(lis, libraryName, scriptUrl, ".wasm.js",true);
            }
            else {
                loadWasm(lis, libraryName, prefix, ".wasm.js",true);
            }
        }
    }

    private static void loadWasm(JParserLibraryLoaderListener listener, String libraryName, String prefix, String postfix, boolean fallback) {
        loadScript(libraryName, new JParserLibraryLoaderListener() {
            @Override
            public void onLoad(boolean isSuccess, Exception e) {
                if(isSuccess) {
                    // Wasm requires to setup wasm first
                    String fullLibName = libraryName + "OnInit";
                    setOnLoadInit(fullLibName, () -> {
                        loadedLibraries.add(libraryName);
                        listener.onLoad(true, null);
                    });
                }
                else {
                    if(fallback) {
                        // Fallback to javascript
                        loadJS(listener, libraryName, prefix);
                    }
                    else {
                        listener.onLoad(false, e);
                    }
                }
            }
        }, prefix, postfix);
    }

    private static void loadJS(JParserLibraryLoaderListener listener, String libraryName, String prefix) {
        loadScript(libraryName, new JParserLibraryLoaderListener() {
            @Override
            public void onLoad(boolean isSuccess, Exception e) {
                if(isSuccess) {
                    listener.onLoad(true, null);
                }
                else {
                    listener.onLoad(false, e);
                }
            }
        }, prefix, ".js");
    }

    private static void loadScript(String libraryName, JParserLibraryLoaderListener listener, String prefix, String postfix) {
        String url = prefix + libraryName + postfix;
        Window current = Window.current();
        HTMLDocument document = current.getDocument();
        HTMLScriptElement scriptElement = (HTMLScriptElement)document.createElement("script");
        scriptElement.addEventListener("load", event -> {
            listener.onLoad(true, null);
        });
        scriptElement.addEventListener("error", (error) -> {
            String str =  prefix + libraryName;
            listener.onLoad(false, new ScriptException("Failed to load .wasm.js or .js script: " + str));
        });
        scriptElement.setSrc(url);
        document.getBody().appendChild(scriptElement);
    }

    @JSFunctor
    public interface OnInitFunction extends org.teavm.jso.JSObject {
        void onInit();
    }

    @JSBody(params = { "libraryName", "onInitFunction" }, script = "window[libraryName] = onInitFunction;")
    private static native void setOnLoadInit(String libraryName, OnInitFunction onInitFunction);
}