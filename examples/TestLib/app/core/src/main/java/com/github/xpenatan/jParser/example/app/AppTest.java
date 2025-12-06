package com.github.xpenatan.jParser.example.app;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.github.xpenatan.jParser.example.testlib.TestLibLoader;
import com.github.xpenatan.jparser.idl.IDLLoader;

public class AppTest extends ApplicationAdapter {

    private boolean init = false;
    private SpriteBatch batch;
    private BitmapFont font;
    private boolean testPass = false;
    private Color color = Color.GRAY;

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

        batch = new SpriteBatch();
        font = new BitmapFont();
    }

    @Override
    public void render() {
        ScreenUtils.clear(color);
        if(init) {
            init = false;
            runTests();
            return;
        }
        batch.begin();
        font.draw(batch, "Test Pass " + testPass, 100, Gdx.graphics.getHeight()/2f);
        batch.end();
        if(Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            runTests();
        }
    }

    private void runTests() {
        testPass = TestLib.test();
        color = testPass ? Color.LIME : Color.RED;
    }
}