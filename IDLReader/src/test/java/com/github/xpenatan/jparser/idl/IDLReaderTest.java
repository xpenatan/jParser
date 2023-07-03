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
    public void test_NoDeleteClassTest_static_method() {
        IDLReader idlReader = IDLReader.readIDL("src\\test\\resources\\idl\\Test.idl");
        IDLClass idlClass = idlReader.getClass("NoDeleteClassTest");
        IDLMethod idlMethod = idlClass.methods.get(0);
        Assert.assertEquals("GetNormalValueClass", idlMethod.name);
        Assert.assertEquals("NormalClassTest", idlMethod.returnType);
        Assert.assertEquals(true, idlMethod.isReturnValue);
    }

    @Test
    public void test_NoDeleteClassTest_attributes() {
        IDLReader idlReader = IDLReader.readIDL("src\\test\\resources\\idl\\Test.idl");
        IDLClass idlClass = idlReader.getClass("NoDeleteClassTest");
        IDLAttribute idlAttribute = idlClass.attributes.get(0);
        Assert.assertEquals("ReadOnlyInt", idlAttribute.name);
        Assert.assertTrue(idlClass.attributes.get(0).isStatic);
        Assert.assertTrue(idlClass.attributes.get(0).isReadOnly);
        Assert.assertEquals("int", idlClass.attributes.get(0).type);
        Assert.assertTrue(idlClass.attributes.get(1).isConst);
        Assert.assertEquals("constNormalClass", idlClass.attributes.get(1).name);
        Assert.assertEquals("NormalClassTest", idlClass.attributes.get(1).type);
        Assert.assertEquals("valueNormalClass", idlClass.attributes.get(2).name);
        Assert.assertTrue(idlClass.attributes.get(2).isValue);
        Assert.assertEquals("NormalClassTest", idlClass.attributes.get(2).type);
        Assert.assertEquals("floatAttribute", idlClass.attributes.get(3).name);
        Assert.assertEquals("float", idlClass.attributes.get(3).type);
        Assert.assertEquals("NormalClassTest", idlClass.attributes.get(2).type);
        Assert.assertEquals("boolAttribute", idlClass.attributes.get(4).name);
        Assert.assertEquals("boolean", idlClass.attributes.get(4).type);
        Assert.assertEquals("arrayBoolean", idlClass.attributes.get(6).name);
        Assert.assertEquals("boolean[]", idlClass.attributes.get(6).type);
        Assert.assertEquals("arrayInt", idlClass.attributes.get(7).name);
        Assert.assertEquals("int[]", idlClass.attributes.get(7).type);
        Assert.assertEquals("anyObject", idlClass.attributes.get(8).name);
        Assert.assertEquals("any", idlClass.attributes.get(8).type);
        Assert.assertTrue(idlClass.attributes.get(8).isAny);
    }

    @Test
    public void test_NoDeleteClassTest_method_with_params() {
        IDLReader idlReader = IDLReader.readIDL("src\\test\\resources\\idl\\Test.idl");
        IDLClass idlClass = idlReader.getClass("NoDeleteClassTest");
        IDLMethod idlMethod = idlClass.methods.get(1);
        Assert.assertEquals("getExtendClass", idlMethod.name);
        Assert.assertEquals("ExtendClassTest", idlMethod.returnType);
        Assert.assertEquals("float", idlMethod.parameters.get(0).type);
        Assert.assertEquals("int", idlMethod.parameters.get(1).type);
        Assert.assertEquals("NormalClassTest", idlMethod.parameters.get(2).type);
        Assert.assertEquals("normalClass", idlMethod.parameters.get(2).name);
        Assert.assertEquals("float[]", idlMethod.parameters.get(3).type);
        Assert.assertTrue(idlMethod.parameters.get(3).isArray);
        Assert.assertEquals("vertices", idlMethod.parameters.get(3).name);
        Assert.assertTrue(idlMethod.parameters.get(2).isRef);
        Assert.assertTrue(idlMethod.parameters.get(2).isConst);
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

