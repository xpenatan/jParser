package com.github.xpenatan.jparser.idl;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class IDLReaderTest {


    @BeforeClass
    public static void setUp() throws Exception {
    }

    @Test
    public void test_NoDeleteClassTest_not_null() {
        IDLReader idlReader = IDLReader.readIDL("src\\test\\resources\\idl\\Test.idl");
        IDLClass idlClass = idlReader.getClass("NoDeleteClassTest");
        Assert.assertNotNull(idlClass);
    }

    @Test
    public void test_PrefixClassTest_not_null() {
        IDLReader idlReader = IDLReader.readIDL("src\\test\\resources\\idl\\Test.idl");
        IDLClass idlClass = idlReader.getClass("PrefixClassTest");
        Assert.assertNotNull(idlClass);
    }

    @Test
    public void test_PrefixClassTest_should_contains_prefix() {
        IDLReader idlReader = IDLReader.readIDL("src\\test\\resources\\idl\\Test.idl");
        IDLClass idlClass = idlReader.getClass("PrefixClassTest");
        Assert.assertEquals("MyPrefix::", idlClass.classHeader.prefixName);
    }

    @Test
    public void test_PrefixNoDeleteClassTest_not_null() {
        IDLReader idlReader = IDLReader.readIDL("src\\test\\resources\\idl\\Test.idl");
        IDLClass idlClass = idlReader.getClass("PrefixNoDeleteClassTest");
        Assert.assertNotNull(idlClass);
    }

    @Test
    public void test_PrefixNoDeleteClassTest_contains_prefix_and_no_delete() {
        IDLReader idlReader = IDLReader.readIDL("src\\test\\resources\\idl\\Test.idl");
        IDLClass idlClass = idlReader.getClass("PrefixNoDeleteClassTest");
        Assert.assertEquals("MyPrefix::", idlClass.classHeader.prefixName);
        Assert.assertEquals(true, idlClass.classHeader.isNoDelete);
    }

    @Test
    public void test_NoDeletePrefixClassTest_not_null() {
        IDLReader idlReader = IDLReader.readIDL("src\\test\\resources\\idl\\Test.idl");
        IDLClass idlClass = idlReader.getClass("NoDeletePrefixClassTest");
        Assert.assertNotNull(idlClass);
    }

    @Test
    public void test_NoDeletePrefixClassTest_contains_prefix_and_no_delete() {
        IDLReader idlReader = IDLReader.readIDL("src\\test\\resources\\idl\\Test.idl");
        IDLClass idlClass = idlReader.getClass("NoDeletePrefixClassTest");
        Assert.assertEquals("MyPrefix::", idlClass.classHeader.prefixName);
        Assert.assertEquals(true, idlClass.classHeader.isNoDelete);
    }

    @Test
    public void test_ConcreteJSImplementationClassTest_not_null() {
        IDLReader idlReader = IDLReader.readIDL("src\\test\\resources\\idl\\Test.idl");
        IDLClass idlClass = idlReader.getClass("ConcreteJSImplementationClassTest");
        Assert.assertNotNull(idlClass);
    }

    @Test
    public void test_ConcreteJSImplementationClassTest_contains_implementation() {
        IDLReader idlReader = IDLReader.readIDL("src\\test\\resources\\idl\\Test.idl");
        IDLClass idlClass = idlReader.getClass("ConcreteJSImplementationClassTest");
        Assert.assertEquals("NoDeletePrefixClassTest", idlClass.classHeader.jsImplementation);
    }

    @Test
    public void test_NormalClassTest_not_null() {
        IDLReader idlReader = IDLReader.readIDL("src\\test\\resources\\idl\\Test.idl");
        IDLClass idlClass = idlReader.getClass("NormalClassTest");
        Assert.assertNotNull(idlClass);
    }

    @Test
    public void test_ExtendClassTest_not_null() {
        IDLReader idlReader = IDLReader.readIDL("src\\test\\resources\\idl\\Test.idl");
        IDLClass idlClass = idlReader.getClass("ExtendClassTest");
        Assert.assertNotNull(idlClass);
    }


}

