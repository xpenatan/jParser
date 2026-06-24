package com.github.xpenatan.jParser.example.app.desktopc;

import com.github.xpenatan.jParser.example.app.TestLib;
import com.github.xpenatan.jParser.example.app.tests.AttributeTest;
import com.github.xpenatan.jParser.example.app.tests.BufferTest;
import com.github.xpenatan.jParser.example.app.tests.CallbackTest;
import com.github.xpenatan.jParser.example.app.tests.ConstructorTest;
import com.github.xpenatan.jParser.example.app.tests.CustomTest;
import com.github.xpenatan.jParser.example.app.tests.EnumTest;
import com.github.xpenatan.jParser.example.app.tests.ExceptionTest;
import com.github.xpenatan.jParser.example.app.tests.MethodTest;
import com.github.xpenatan.jParser.example.app.tests.NamespaceTest;
import com.github.xpenatan.jParser.example.app.tests.OperatorTest;
import com.github.xpenatan.jParser.example.app.tests.PrimitiveTest;
import com.github.xpenatan.jParser.example.testlib.TestLibLoader;
import com.github.xpenatan.jparser.runtime.RuntimeLoader;

public class TeaVMCHeadlessMain {

    public static void main(String[] args) {
        RuntimeLoader.init(null);
        TestLibLoader.init(null);

        if(!TestLib.test()) {
            printFailures();
            throw new AssertionError("TestLib TeaVM C tests failed");
        }
        System.out.println("TestLib TeaVM C tests passed");
    }

    private static void printFailures() {
        if(!new AttributeTest().test()) throw new AssertionError("AttributeTest");
        if(!new BufferTest().test()) throw new AssertionError("BufferTest");
        if(!new CallbackTest().test()) throw new AssertionError("CallbackTest");
        if(!new ConstructorTest().test()) throw new AssertionError("ConstructorTest");
        if(!new CustomTest().test()) throw new AssertionError("CustomTest");
        if(!new EnumTest().test()) throw new AssertionError("EnumTest");
        if(!new ExceptionTest().test()) throw new AssertionError("ExceptionTest");
        if(!new MethodTest().test()) throw new AssertionError("MethodTest");
        if(!new NamespaceTest().test()) throw new AssertionError("NamespaceTest");
        if(!new OperatorTest().test()) throw new AssertionError("OperatorTest");
        if(!new PrimitiveTest().test()) throw new AssertionError("PrimitiveTest");
    }
}
