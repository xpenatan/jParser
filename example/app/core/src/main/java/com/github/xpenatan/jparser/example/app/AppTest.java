package com.github.xpenatan.jparser.example.app;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.github.xpenatan.jparser.example.testlib.TestLibLoader;

public class AppTest extends ApplicationAdapter {
    private boolean init = false;

    private SpriteBatch batch;
    private BitmapFont font;

    boolean testPass = false;

    Color color = Color.GRAY;

    @Override
    public void create() {
        TestLibLoader.init(new Runnable() {
            @Override
            public void run() {
                init = true;
            }
        });

        batch = new SpriteBatch();
        font = new BitmapFont();
    }


    @Override
    public void render() {
        ScreenUtils.clear(color);

        if(init) {
            init = false;
            testPass = TestLib.test();
            color = testPass ? Color.GREEN : Color.RED;
            return;
        }

        batch.begin();
        font.draw(batch, "Test Pass " + testPass, 100, Gdx.graphics.getHeight()/2f);
        batch.end();
    }
}