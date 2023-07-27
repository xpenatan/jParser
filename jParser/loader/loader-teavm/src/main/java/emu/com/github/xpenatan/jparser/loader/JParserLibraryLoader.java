package emu.com.github.xpenatan.jparser.loader;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import org.teavm.jso.browser.Window;
import org.teavm.jso.dom.html.HTMLDocument;
import org.teavm.jso.dom.html.HTMLElement;

public class JParserLibraryLoader {

    private int test =31231;

    public JParserLibraryLoader() {
    }

    public JParserLibraryLoader(String nativesJar) {
    }

    public void load(String libraryName) {
        load(libraryName, null);
    }

    public void load(String libraryName01, String libraryName02) {
        if(libraryName02 != null) {

        }
        if(libraryName01 != null) {
            libraryName01 = libraryName01 + ".js";
            System.out.println("Loading JS script: " + libraryName01);
            String script= loadResource(libraryName01);
            Window current = Window.current();
            HTMLDocument document = current.getDocument();
            HTMLElement scriptElement = document.createElement("script");
            scriptElement.appendChild(document.createTextNode(script));
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
