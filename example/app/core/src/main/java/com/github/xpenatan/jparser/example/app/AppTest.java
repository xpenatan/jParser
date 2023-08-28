package com.github.xpenatan.jparser.example.app;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.github.xpenatan.jparser.example.lib.EnumLib;
import com.github.xpenatan.jparser.example.lib.ExampleLib;
import com.github.xpenatan.jparser.example.lib.NormalClass;
import com.github.xpenatan.jparser.example.lib.idl.helper.FloatArray;

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

        int version = normalClass.getVersion();
        System.out.println("Version " + version);

        ret1 = normalClass.addIntValue(a1, b1);
        System.out.println("addIntValue " + a1 + " + " + b1 + " = " + ret1);

        FloatArray array = new FloatArray(1);
        array.setValue(0, 10);
        float value = array.getValue(0);
        System.out.println("VALUE: " + value);

        System.out.println("ENUM FIRST: " + EnumLib.FIRST);
        System.out.println("ENUM SECOND: " + EnumLib.SECOND);
        System.out.println("ENUMPARAM FIRST: " + normalClass.enumParam(EnumLib.FIRST));
        System.out.println("ENUMPARAM SECOND: " + normalClass.enumParam(EnumLib.SECOND));
        normalClass.enumVoidParam(EnumLib.FIRST);
        System.out.println("ENUM Return FIRST: " + normalClass.enumReturn(1));
        System.out.println("ENUM Return SECOND: " + normalClass.enumReturn(2));
        System.out.println("ENUM Return DEFAULT: " + normalClass.enumReturn(99));

        normalClass.printText(10, "printText HELLO");
        FloatArray floatArray = new FloatArray(1);
        long pointer = floatArray.getPointer();
        System.out.println("pointer: " + pointer);
        normalClass.setArray(floatArray);
        System.out.println("setArray: " + floatArray.getValue(0));
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