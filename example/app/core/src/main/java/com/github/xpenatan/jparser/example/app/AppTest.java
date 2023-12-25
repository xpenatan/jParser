package com.github.xpenatan.jparser.example.app;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.github.xpenatan.jparser.example.lib.EnumInNamespace;
import com.github.xpenatan.jparser.example.lib.EnumLib;
import com.github.xpenatan.jparser.example.lib.EnumTwoLib;
import com.github.xpenatan.jparser.example.lib.EnumWithinClass;
import com.github.xpenatan.jparser.example.lib.ExampleLibLoader;
import com.github.xpenatan.jparser.example.lib.NormalClass;
import com.github.xpenatan.jparser.example.lib.OperatorClass;
import com.github.xpenatan.jparser.example.lib.ReturnClass;
import com.github.xpenatan.jparser.example.lib.idl.helper.IDLFloat;
import com.github.xpenatan.jparser.example.lib.idl.helper.IDLFloatArray;
import com.lib.ext.CustomLib;

public class AppTest extends ApplicationAdapter {
    private boolean init = false;

    private SpriteBatch batch;
    private BitmapFont font;

    private int a1 = 1;
    private int b1 = 1;
    private int ret1;

    @Override
    public void create() {
        ExampleLibLoader.init(new Runnable() {
            @Override
            public void run() {
                initLib();
            }
        });

        batch = new SpriteBatch();
        font = new BitmapFont();
    }

    private void initLib() {
        if(init) {
            return;
        }
        init = true;

        NormalClass normalClass = new NormalClass();

        int version = normalClass.getVersion();
        System.out.println("Version " + version);

        ret1 = normalClass.addIntValue(a1, b1);
        System.out.println("addIntValue " + a1 + " + " + b1 + " = " + ret1);

        IDLFloatArray array = new IDLFloatArray(1);
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
        System.out.println("EnumWithinClass e_val: " + EnumWithinClass.e_val);
        System.out.println("EnumInNamespace e_namespace_val: " + EnumInNamespace.e_namespace_val);

        ReturnClass returnValueObject = normalClass.getReturnValueObject();
        System.out.println("returnValueObject: " + returnValueObject.get_value());

        normalClass.printText(10, "printText HELLO");
        IDLFloat floatArray = IDLFloat.TMP_1;
        long pointer = floatArray.getPointer();
        System.out.println("pointer: " + pointer);
        normalClass.setArray(floatArray);
        System.out.println("setArray: " + floatArray.getValue());
        System.out.println("EnumTwoLib THIRD: " + EnumTwoLib.EnumTwoLib_THIRD);
        System.out.println("EnumTwoLib FOURTH: " + EnumTwoLib.EnumTwoLib_FOURTH);
        System.out.println("NormalClass.subIntValue: " + NormalClass.subIntValue(2, 1));

        OperatorClass operatorClass1 = new OperatorClass();
        operatorClass1.set_value(41);
        OperatorClass operatorClass2 = new OperatorClass();
        operatorClass2.set_value(3);
        operatorClass1.copy(operatorClass2);

        System.out.println("operatorClass1 copy: " + operatorClass1.get_value());

        testPrimitive();

        CustomLib.print();
    }

    private void testPrimitive() {
        System.out.println("########## TESTING ATTRIBUTES ##########");

        NormalClass.set_hiddenInt_static(22);
        int hiddenIntStatic = NormalClass.get_hiddenInt_static();
        System.out.println("hiddenIntStatic: " + hiddenIntStatic);

        ReturnClass nullPointerReturnClassStatic = NormalClass.get_nullPointerReturnClass_static();
        System.out.println("nullPointerReturnClassStatic: " + nullPointerReturnClassStatic);

//        ReturnClass pointerReturnClassStatic = NormalClass.get_pointerReturnClass_static();
//        pointerReturnClassStatic.set_value(51);
//        System.out.println("pointerReturnClassStatic: " + pointerReturnClassStatic.get_value());

        ReturnClass valueReturnClassStatic = NormalClass.get_valueReturnClass_static();
        System.out.println("valueReturnClassStatic: " + valueReturnClassStatic.get_value());

        NormalClass normalClass = new NormalClass();

        normalClass.set_hiddenInt(4);
        int hiddenInt = normalClass.get_hiddenInt();
        System.out.println("hiddenInt: " + hiddenInt);

        ReturnClass pointerReturnClass = normalClass.get_pointerReturnClass();
        pointerReturnClass.set_value(11);
        System.out.println("pointerReturnClass: " + pointerReturnClass.get_value());

        ReturnClass valueReturnClass = normalClass.get_valueReturnClass();
        valueReturnClass.set_value(12);
        System.out.println("valueReturnClass: " + valueReturnClass.get_value());

        ReturnClass nullPointerReturnClass = normalClass.get_nullPointerReturnClass();
        System.out.println("nullPointerReturnClass: " + nullPointerReturnClass);
    }

    @Override
    public void render() {
        ScreenUtils.clear(0.4f, 0.4f, 0.4f, 1);

        if(!init) {
            return;
        }

        batch.begin();
        font.draw(batch, "addIntValue " + a1 + " + " + b1 + " = " + ret1, 100, 100);
        batch.end();
    }
}