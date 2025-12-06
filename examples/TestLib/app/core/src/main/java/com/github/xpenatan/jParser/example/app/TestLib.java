package com.github.xpenatan.jParser.example.app;

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
import com.github.xpenatan.jParser.example.testlib.CallbackClassManual;
import com.github.xpenatan.jParser.idl.IDLBase;
import java.util.ArrayList;

public class TestLib {

    public static boolean test() {

        ArrayList<String> logs = new ArrayList<>();
        boolean allTestsPassed = true;
        for(CodeTest setupTest : setupTests()) {
            boolean result = setupTest.test();
            String testName = setupTest.getClass().getSimpleName();
            logs.add(testName + ": " + result);
            allTestsPassed = allTestsPassed && result;
        }
        System.out.println("\n#### Tests:");
        for(String log : logs) {
            System.out.println(log);
        }
        return allTestsPassed;
    }

    private static ArrayList<CodeTest> setupTests() {
        ArrayList<CodeTest> tests = new ArrayList<>();
        tests.add(new AttributeTest());
        tests.add(new BufferTest());
        tests.add(new CallbackTest());
        tests.add(new ConstructorTest());
        tests.add(new CustomTest());
        tests.add(new EnumTest());
        tests.add(new ExceptionTest());
        tests.add(new MethodTest());
        tests.add(new NamespaceTest());
        tests.add(new OperatorTest());
        tests.add(new PrimitiveTest());
        return tests;
    }
}