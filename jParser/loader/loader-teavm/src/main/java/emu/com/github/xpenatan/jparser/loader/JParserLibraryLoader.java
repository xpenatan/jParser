package emu.com.github.xpenatan.jparser.loader;

import com.github.xpenatan.jparser.loader.JParserLibraryLoaderListener;
import java.util.HashSet;
import javax.script.ScriptException;
import org.teavm.jso.JSBody;
import org.teavm.jso.JSFunctor;
import org.teavm.jso.browser.Window;
import org.teavm.jso.dom.html.HTMLDocument;
import org.teavm.jso.dom.html.HTMLScriptElement;

public class JParserLibraryLoader {

    public static String PREFIX = "";

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
        loadWasm(libraryName, listener, PREFIX);
    }

    private static void loadWasm(final String libraryName, JParserLibraryLoaderListener listener, String prefix) {
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
                    // Fallback to javascript
                    loadJS(libraryName, listener, prefix);
                }
            }
        }, prefix, ".wasm.js");
    }

    private static void loadJS(final String libraryName, JParserLibraryLoaderListener listener, String prefix) {
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