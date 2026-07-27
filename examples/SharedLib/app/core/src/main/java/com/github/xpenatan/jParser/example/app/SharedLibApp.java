package com.github.xpenatan.jParser.example.app;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.github.xpenatan.jParser.loader.JParserLibraryLoaderListener;
import com.github.xpenatan.jParser.loader.JParserNativeBundleLoader;
import com.github.xpenatan.jparser.runtime.RuntimeLoader;
import libA.LibALoader;
import libB.LibBLoader;

public class SharedLibApp extends ApplicationAdapter {
    private final String nativeBundleName;
    private boolean init = false;

    private SpriteBatch batch;
    private BitmapFont font;

    boolean testPass = false;

    Color color = Color.GRAY;

    public SharedLibApp() {
        this(null);
    }

    /**
     * Creates a fat-mode application when {@code nativeBundleName} is non-null.
     * Fat mode deliberately bypasses every generated per-library loader.
     */
    public SharedLibApp(String nativeBundleName) {
        this.nativeBundleName = nativeBundleName;
    }

    @Override
    public void create() {
        JParserLibraryLoaderListener loaded = new JParserLibraryLoaderListener() {
            @Override
            public void onLoad(boolean isSuccess, Throwable throwable) {
                if(throwable != null) {
                    throwable.printStackTrace();
                    return;
                }
                init = isSuccess;
            }
        };
        if(nativeBundleName != null && !nativeBundleName.trim().isEmpty()) {
            JParserNativeBundleLoader.load(nativeBundleName.trim(), loaded);
        }
        else {
            loadStandaloneBindings(loaded);
        }

        batch = new SpriteBatch();
        font = new BitmapFont();
    }

    private void loadStandaloneBindings(JParserLibraryLoaderListener loaded) {
        RuntimeLoader.init((runtimeSuccess, runtimeFailure) -> {
            if(runtimeFailure != null) {
                loaded.onLoad(false, runtimeFailure);
                return;
            }
            LibALoader.init((libASuccess, libAFailure) -> {
                if(libAFailure != null) {
                    loaded.onLoad(false, libAFailure);
                    return;
                }
                LibBLoader.init((libBSuccess, libBFailure) ->
                        loaded.onLoad(libBSuccess, libBFailure));
            });
        });
    }

    @Override
    public void render() {
        ScreenUtils.clear(color);

        if(init) {
            init = false;
            testPass = SharedLib.test();
            System.out.println("SharedLib Test Pass " + testPass);
            color = testPass ? Color.LIME : Color.RED;
            return;
        }

        batch.begin();
        font.draw(batch, "SharedLib Test Pass " + testPass, 100, Gdx.graphics.getHeight()/2f);
        batch.end();
    }
}
