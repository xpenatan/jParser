package com.github.xpenatan.jparser.example.app;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

public class Main {

    public static void main(String[] args) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.vSyncEnabled = false;
        config.foregroundFPS = 60;
        new LwjglApplication(new AppTest(), config);
    }
}