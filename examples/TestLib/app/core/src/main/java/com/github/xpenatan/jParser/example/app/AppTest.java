package com.github.xpenatan.jParser.example.app;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.github.xpenatan.jParser.example.testlib.TestLibLoader;
import com.github.xpenatan.jParser.loader.JParserLibraryLoaderListener;
import com.github.xpenatan.jParser.loader.JParserNativeBundleLoader;
import com.github.xpenatan.jparser.runtime.RuntimeLoader;

public class AppTest extends ApplicationAdapter {

    private final String nativeBundleName;
    private boolean init = false;
    private SpriteBatch batch;
    private BitmapFont font;
    private boolean testPass = false;
    private Color color = Color.GRAY;

    public AppTest() {
        this(null);
    }

    /**
     * Creates a fat-mode application when {@code nativeBundleName} is non-null.
     */
    public AppTest(String nativeBundleName) {
        this.nativeBundleName = nativeBundleName;
    }

    @Override
    public void create() {
        JParserLibraryLoaderListener loaded = (isSuccess, throwable) -> {
            if(throwable != null) {
                throwable.printStackTrace();
                return;
            }
            init = isSuccess;
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
            TestLibLoader.init(loaded);
        });
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
        font.draw(batch, "TestLib Test Pass " + testPass, 100, Gdx.graphics.getHeight()/2f);
        batch.end();
        if(Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            runTests();
        }
    }

    private void runTests() {
        testPass = TestLib.test();
        System.out.println("TestLib Test Pass " + testPass);
        color = testPass ? Color.LIME : Color.RED;
    }
}
