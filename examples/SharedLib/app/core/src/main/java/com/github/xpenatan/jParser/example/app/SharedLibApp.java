package com.github.xpenatan.jParser.example.app;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.github.xpenatan.jParser.loader.JParserLibraryLoaderListener;
import com.github.xpenatan.jparser.idl.IDLLoader;
import libA.LibALoader;
import libB.LibBLoader;

public class SharedLibApp extends ApplicationAdapter {
    private boolean init = false;

    private SpriteBatch batch;
    private BitmapFont font;

    boolean testPass = false;

    Color color = Color.GRAY;

    @Override
    public void create() {
        IDLLoader.init(new JParserLibraryLoaderListener() {
            @Override
            public void onLoad(boolean idl_isSuccess, Exception idl_e) {
                if(idl_e != null) {
                    idl_e.printStackTrace();
                    return;
                }
                LibALoader.init((libA_isSuccess, libA_e) -> {
                    if(libA_e != null) {
                        libA_e.printStackTrace();
                        return;
                    }
                    LibBLoader.init((libB_isSuccess, libB_e) -> {
                        if(libB_e != null) {
                            libB_e.printStackTrace();
                            return;
                        }
                        init = true;
                    });
                });
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
            testPass = SharedLibTest.test();
            color = testPass ? Color.LIME : Color.RED;
            return;
        }

        batch.begin();
        font.draw(batch, "Test Pass " + testPass, 100, Gdx.graphics.getHeight()/2f);
        batch.end();
    }
}