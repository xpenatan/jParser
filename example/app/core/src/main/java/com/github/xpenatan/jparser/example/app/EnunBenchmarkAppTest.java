package com.github.xpenatan.jparser.example.app;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.ScreenUtils;
import com.github.xpenatan.jparser.example.testlib.TestLibLoader;

public class EnunBenchmarkAppTest extends ApplicationAdapter {
    private boolean init = false;

    @Override
    public void create() {
        TestLibLoader.init((isSuccess, e) -> {
            if(e != null) {
                e.printStackTrace();
            }
            init = isSuccess;
        });
    }

    @Override
    public void render() {
        if(init) {
            init = false;
            EnumBenchmark.test();
        }
        ScreenUtils.clear(Color.GREEN);
    }
}