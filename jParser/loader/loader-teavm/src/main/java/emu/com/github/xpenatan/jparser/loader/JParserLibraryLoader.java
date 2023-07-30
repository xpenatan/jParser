package emu.com.github.xpenatan.jparser.loader;

import org.teavm.jso.browser.Window;
import org.teavm.jso.dom.events.Event;
import org.teavm.jso.dom.events.EventListener;
import org.teavm.jso.dom.html.HTMLDocument;
import org.teavm.jso.dom.html.HTMLScriptElement;

public class JParserLibraryLoader {

    public JParserLibraryLoader() {
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

        }
        if(libraryName01 != null) {
            libraryName01 = "assets/" + libraryName01 + ".js";
            System.out.println("Loading JS script: " + libraryName01);
            Window current = Window.current();
            HTMLDocument document = current.getDocument();
            HTMLScriptElement scriptElement = (HTMLScriptElement)document.createElement("script");
            scriptElement.setSrc(libraryName01);
            scriptElement.addEventListener("load", new EventListener<Event>() {
                @Override
                public void handleEvent(Event evt) {
                    if(runnable != null) {
                        runnable.run();
                    }
                }
            });
            document.getBody().appendChild(scriptElement);
        }
    }
}
