package com.github.xpenatan.jParser.example.app.tests;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.github.xpenatan.jParser.example.app.CodeTest;
import com.github.xpenatan.jParser.example.testlib.CallbackExceptionManual;
import com.github.xpenatan.jParser.example.testlib.TestExceptionManual;

public class ExceptionTest implements CodeTest {
    private static boolean testExceptionManual() {
        if(Gdx.app.getType() != Application.ApplicationType.WebGL) {
            // TODO add JNI support
            return true;
        }
        boolean ret = true;
        try {
            ret = false;

            CallbackExceptionManual callback = new CallbackExceptionManual() {
                @Override
                public void internal_callJava() {
                    System.out.println("CALL_JAVA");
                    throw new IllegalStateException("This is expected");
                }
            };

            TestExceptionManual exceptionManual = new TestExceptionManual();
            exceptionManual.callJavaMethod(callback);
        }
        catch(Throwable t) {
            t.printStackTrace();
            ret = true;
        }

        try {
            ret = false;
            // This test is calling a c++ method and the C++ code tries to set a value to a null object pointer.
            // It will trigger an exception in native side.
            TestExceptionManual exceptionManual = new TestExceptionManual();
            int value = exceptionManual.setDataToNullPointer();
            System.out.println("Value: " + value);
        }
        catch(Throwable t) {
            // Javascript/Wasm catches the error in native side automatically. C++ does not, need to implement.
            t.printStackTrace();
            ret = true;
        }
        return ret;
    }

    @Override
    public boolean test() {
        return testExceptionManual();
    }
}