package emu.com.github.xpenatan.jparser.loader;

import com.badlogic.gdx.utils.SharedLibraryLoader;
import java.io.File;

public class JParserLibraryLoader {

    private int test =31231;

    public JParserLibraryLoader() {
    }

    public JParserLibraryLoader(String nativesJar) {
    }

    public void load(String libraryName) {
        System.out.println("HELLO WORLD: " + test);
    }

    public void load(String libraryName01, String libraryName02) {

        test++;
        System.out.println("HELLO WORLD2: " + test);
    }
}
