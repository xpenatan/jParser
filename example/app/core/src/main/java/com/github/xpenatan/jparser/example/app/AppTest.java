package com.github.xpenatan.jparser.example.app;

import com.badlogic.gdx.ApplicationAdapter;
import com.github.xpenatan.jparser.example.NormalClass;
import com.github.xpenatan.jparser.loader.JParserLibraryLoader;

public class AppTest extends ApplicationAdapter {
    @Override
    public void create() {
        JParserLibraryLoader libraryLoader = new JParserLibraryLoader();
        libraryLoader.load("exampleLib");

        NormalClass normalClass = new NormalClass();
        int a = 1;
        int b = 1;
        int ret = normalClass.addIntValue(a, b);
        System.out.println(a + " + "  + b + " = " + ret);
    }

    @Override
    public void render() {

    }
}