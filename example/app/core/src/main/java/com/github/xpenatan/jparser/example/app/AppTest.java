package com.github.xpenatan.jparser.example.app;

import com.badlogic.gdx.ApplicationAdapter;
import com.github.xpenatan.jparser.example.lib.ExampleLib;
import com.github.xpenatan.jparser.example.lib.NormalClass;

public class AppTest extends ApplicationAdapter {
    @Override
    public void create() {
        ExampleLib.init(new Runnable() {
            @Override
            public void run() {
                NormalClass normalClass = new NormalClass();
                int a = 1;
                int b = 1;
                int ret = normalClass.addIntValue(a, b);
                System.out.println("addIntValue " + a + " + "  + b + " = " + ret);

                int i = normalClass.hiddenInt();
                System.out.println("hiddenInt() before: " + i);
                normalClass.hiddenInt(39);
                int i2 = normalClass.hiddenInt();
                System.out.println("hiddenInt() after: " + i2);
            }
        });
    }

    @Override
    public void render() {
//        System.out.println("1111");
    }
}