package com.github.xpenatan.jParser.example.app;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

public class NativeBridgeFpsBenchmarkMain {

    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("FPS Benchmark");
        config.useVsync(false);
        config.setForegroundFPS(0);
        new Lwjgl3Application(new NativeBridgeFpsBenchmarkApp(), config);
    }
}

