package com.github.xpenatan.jParser.example.app;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.ScreenUtils;
import com.github.xpenatan.jParser.example.testlib.TestLibLoader;
import com.github.xpenatan.jparser.idl.IDLLoader;

public class NativeBridgeFpsBenchmarkApp extends ApplicationAdapter {

    private boolean init = false;
    private boolean running = false;
    private NativeBridgeFpsBenchmark benchmark;

    @Override
    public void create() {
        IDLLoader.init((idl_isSuccess, idl_e) -> {
            if(idl_e != null) {
                idl_e.printStackTrace();
                return;
            }
            TestLibLoader.init((isSuccess, e) -> {
                if(e != null) {
                    e.printStackTrace();
                }
                init = isSuccess;
            });
        });
    }

    @Override
    public void render() {
        ScreenUtils.clear(Color.DARK_GRAY);

        if(init && !running) {
            init = false;
            running = true;
            benchmark = new NativeBridgeFpsBenchmark();
            benchmark.start();
        }

        if(running) {
            boolean done = benchmark.update();
            if(done) {
                Gdx.app.exit();
            }
        }
    }
}

