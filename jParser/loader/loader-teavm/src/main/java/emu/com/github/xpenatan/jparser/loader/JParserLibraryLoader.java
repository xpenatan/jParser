package emu.com.github.xpenatan.jparser.loader;

import com.github.xpenatan.jmultiplatform.core.JMultiplatform;
import com.github.xpenatan.jparser.loader.JParserLibraryLoaderListener;
import com.github.xpenatan.jparser.loader.JParserLibraryLoaderPlatform;
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

    public static void load(String libraryName, JParserLibraryLoaderListener listener) {
        loadInternal(libraryName, listener);
    }

    private static void loadInternal(String libraryName, JParserLibraryLoaderListener listener) {
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

        String scriptPath = JMultiplatform.getInstance().getMap().getObject(JParserLibraryLoaderPlatform.PLATFORM_WEB_SCRIPT_PATH, String.class);
        if(scriptPath != null) {
            if(libraryName.endsWith(".wasm.js")) {
                loadWasm(lis, libraryName, scriptPath, "", false);
            }
            else if(libraryName.endsWith(".js")) {
                loadJS(lis, libraryName, scriptPath);
            }
            else {
                loadWasm(lis, libraryName, scriptPath, ".wasm.js",true);
            }
        }
        else {
            String platformWebScriptPath = JParserLibraryLoaderPlatform.PLATFORM_WEB_SCRIPT_PATH;
            String error = "JMultiplatform " + platformWebScriptPath + " is not set";
            listener.onLoad(false, new ScriptException(error));
        }
    }

    private static void loadWasm(JParserLibraryLoaderListener listener, String libraryName, String prefix, String postfix, boolean fallback) {
        loadScript(libraryName, (isSuccess, e) -> {
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
        }, prefix, postfix);
    }

    private static void loadJS(JParserLibraryLoaderListener listener, String libraryName, String prefix) {
        loadScript(libraryName, (isSuccess, e) -> {
            if(isSuccess) {
                listener.onLoad(true, null);
            }
            else {
                listener.onLoad(false, e);
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