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
            }
        });
    }

    @Override
    public void render() {
//        System.out.println("1111");
    }
}