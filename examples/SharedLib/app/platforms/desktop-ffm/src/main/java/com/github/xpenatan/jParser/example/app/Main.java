package com.github.xpenatan.jParser.example.app;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

public class Main {

    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        String bundleName = System.getProperty("jparser.nativeBundle", "").trim();
        SharedLibApp app = bundleName.isEmpty()
                ? new SharedLibApp()
                : new SharedLibApp(bundleName);
        new Lwjgl3Application(app, config);
    }
}
