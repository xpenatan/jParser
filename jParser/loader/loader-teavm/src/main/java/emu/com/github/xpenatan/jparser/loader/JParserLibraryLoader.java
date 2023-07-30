package emu.com.github.xpenatan.jparser.loader;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
            libraryName01 = libraryName01 + ".js";
            System.out.println("Loading JS script: " + libraryName01);
            Window current = Window.current();
            HTMLDocument document = current.getDocument();
            HTMLScriptElement scriptElement = (HTMLScriptElement)document.createElement("script");
//            scriptElement.setSrc(libraryName01);
            scriptElement.addEventListener("load", new EventListener<Event>() {
                @Override
                public void handleEvent(Event evt) {
                    System.out.println("AAAAAAAAAAAA");
                    runnable.run();
                }
            });
            String script = loadResource(libraryName01);
            scriptElement.setText(script);
            document.getBody().appendChild(scriptElement);
        }
    }

    private static String loadResource(String name) {
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream(name);
        try {
            ByteArrayOutputStream into = new ByteArrayOutputStream();
            byte[] buf = new byte[4096];
            for (int n; 0 < (n = inputStream.read(buf));) {
                into.write(buf, 0, n);
            }
            into.close();
            return new String(into.toByteArray(), "UTF-8"); // Or whatever encoding
        } catch(IOException e) {
            return null;
        }
    }
}
