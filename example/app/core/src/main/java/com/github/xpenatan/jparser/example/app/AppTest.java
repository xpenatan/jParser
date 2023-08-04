package com.github.xpenatan.jparser.example.app;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.github.xpenatan.jparser.example.lib.ExampleLib;
import com.github.xpenatan.jparser.example.lib.NormalClass;

public class AppTest extends ApplicationAdapter {

    private boolean initLib = false;

    private SpriteBatch batch;
    private BitmapFont font;

    private int a1 = 1;
    private int b1 = 1;
    private int ret1;

    @Override
    public void create() {
        ExampleLib.init(new Runnable() {
            @Override
            public void run() {
                initLib();
            }
        });

        batch = new SpriteBatch();
        font = new BitmapFont();
    }

    private void initLib() {
        initLib = true;

        NormalClass normalClass = new NormalClass();
        ret1 = normalClass.addIntValue(a1, b1);
        System.out.println("addIntValue " + a1 + " + " + b1 + " = " + ret1);
    }

    @Override
    public void render() {
        ScreenUtils.clear(0, 0, 0, 1);
        if(!initLib) {
            return;
        }

        batch.begin();
        font.draw(batch, "addIntValue " + a1 + " + " + b1 + " = " + ret1, 100, 100);
        batch.end();
    }
}