package com.github.xpenatan.jParser.example.app.tests;

import com.github.xpenatan.jParser.example.app.CodeTest;
import com.github.xpenatan.jParser.example.testlib.CallbackClassManual;
import com.github.xpenatan.jParser.idl.IDLBase;

public class CustomTest implements CodeTest {
    @Override
    public boolean test() {
        IDLBase idlVoid = IDLBase.native_new();
        long voidLongData = 1000;
        int voidIntData = 2000;
        idlVoid.native_setAddress(voidLongData);
        System.out.println("voidLongData: " + idlVoid);
        idlVoid.native_setAddress(voidIntData);
        System.out.println("voidIntData: " + idlVoid);

        long code = CallbackClassManual.GetAndroidCode();

        // This is a custom code that will return 1 for android
        System.out.println("Android Code: " + code);

        return true;
    }
}
