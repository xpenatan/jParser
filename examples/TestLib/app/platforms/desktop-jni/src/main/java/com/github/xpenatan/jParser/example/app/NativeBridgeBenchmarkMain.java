package com.github.xpenatan.jParser.example.app;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

public class NativeBridgeBenchmarkMain {

    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Native Bridge Benchmark");
        new Lwjgl3Application(new NativeBridgeBenchmarkApp(), config);
    }
}

