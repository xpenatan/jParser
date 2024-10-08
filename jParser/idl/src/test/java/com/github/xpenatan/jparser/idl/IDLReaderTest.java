package com.github.xpenatan.jparser.idl;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class IDLReaderTest {

    @BeforeClass
    public static void setUp() throws Exception {
    }

    @Test
    public void test_getTag() {
        String tag1 = "[Const, Value] readonly attribute Vec3 mDirection;";
        String tag2= "[Value] attribute SoftBodySharedSettingsSkinWeight[] mWeights;";
        String test1 = IDLHelper.getTags(tag1);
        String test2 = IDLHelper.getTags(tag2);
        Assert.assertEquals("[Const, Value]", test1);
        Assert.assertEquals("[Value]", test2);
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
        Assert.assertEquals(true, idlMethod.isStaticMethod);
        IDLMethod styleMethod = idlClass.getMethod("GetStyle");
        Assert.assertEquals(true, styleMethod.isStaticMethod);
        Assert.assertEquals(true, styleMethod.isReturnRef);
    }

    @Test
    public void test_NoDeleteClassTest_attributes() {
        IDLReader idlReader = IDLReader.readIDL("src\\test\\resources\\idl\\Test.idl");
        IDLClass idlClass = idlReader.getClass("NoDeleteClassTest");
        IDLAttribute idlAttribute = idlClass.attributes.get(0);
        Assert.assertEquals("ReadOnlyInt", idlAttribute.name);
        Assert.assertTrue(idlClass.attributes.get(0).isStatic);
        Assert.assertTrue(idlClass.attributes.get(0).isReadOnly);
        Assert.assertEquals("unsigned long", idlClass.attributes.get(0).idlType);
        Assert.assertTrue(idlClass.attributes.get(1).isConst);
        Assert.assertEquals("constNormalClass", idlClass.attributes.get(1).name);
        Assert.assertEquals("NormalClassTest", idlClass.attributes.get(1).idlType);
        Assert.assertEquals("valueNormalClass", idlClass.attributes.get(2).name);
        Assert.assertTrue(idlClass.attributes.get(2).isValue);
        Assert.assertEquals("NormalClassTest", idlClass.attributes.get(2).idlType);
        Assert.assertEquals("floatAttribute", idlClass.attributes.get(3).name);
        Assert.assertEquals("float", idlClass.attributes.get(3).idlType);
        Assert.assertEquals("NormalClassTest", idlClass.attributes.get(2).idlType);
        Assert.assertEquals("boolAttribute", idlClass.attributes.get(4).name);
        Assert.assertEquals("boolean", idlClass.attributes.get(4).idlType);
        Assert.assertEquals("arrayBoolean", idlClass.attributes.get(6).name);
        Assert.assertEquals("boolean[]", idlClass.attributes.get(6).idlType);
        Assert.assertEquals("arrayInt", idlClass.attributes.get(7).name);
        Assert.assertEquals("unsigned long[]", idlClass.attributes.get(7).idlType);
        Assert.assertEquals("anyObject", idlClass.attributes.get(8).name);
        Assert.assertEquals("any", idlClass.attributes.get(8).idlType);
        Assert.assertTrue(idlClass.attributes.get(8).isAny);
    }

    @Test
    public void test_NoDeleteClassTest_methods() {
        IDLReader idlReader = IDLReader.readIDL("src\\test\\resources\\idl\\Test.idl");
        IDLClass idlClass = idlReader.getClass("NoDeleteClassTest");
        IDLMethod idlMethod = idlClass.methods.get(1);
        Assert.assertEquals("getExtendClass", idlMethod.name);
        Assert.assertEquals("ExtendClassTest", idlMethod.returnType);
        Assert.assertEquals("float", idlMethod.parameters.get(0).idlType);
        Assert.assertEquals("long", idlMethod.parameters.get(1).idlType);
        Assert.assertEquals("NormalClassTest", idlMethod.parameters.get(2).idlType);
        Assert.assertEquals("normalClass", idlMethod.parameters.get(2).name);
        Assert.assertEquals("float[]", idlMethod.parameters.get(3).idlType);
        Assert.assertTrue(idlMethod.parameters.get(3).isArray);
        Assert.assertEquals("vertices", idlMethod.parameters.get(3).name);
        Assert.assertTrue(idlMethod.parameters.get(2).isRef);
        Assert.assertTrue(idlMethod.parameters.get(2).isConst);
        IDLMethod flagsMethod = idlClass.getMethod("GetFlags");
        Assert.assertEquals("unsigned long", flagsMethod.returnType);
        IDLMethod copyNormalClassMethod = idlClass.getMethod("copyNormalClass");
        Assert.assertTrue(copyNormalClassMethod.isReturnRef);
        Assert.assertEquals("=", copyNormalClassMethod.operator);
        IDLMethod multiNormalClassMethod = idlClass.getMethod("multiNormalClass");
        Assert.assertTrue(multiNormalClassMethod.isReturnRef);
        Assert.assertEquals("*=", multiNormalClassMethod.operator);
        IDLMethod addIntMethod = idlClass.getMethod("addInt");
        Assert.assertEquals("+=", addIntMethod.operator);
        IDLMethod subFloatMethod = idlClass.getMethod("subFloat");
        Assert.assertEquals("-=", subFloatMethod.operator);
        IDLMethod getArrayItemMethod = idlClass.getMethod("getArrayItem");
        Assert.assertTrue(getArrayItemMethod.isReturnRef);
        Assert.assertEquals("[]", getArrayItemMethod.operator);
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

    @Test
    public void test_ExtendClassTest_parent_class() {
        IDLReader idlReader = IDLReader.readIDL("src\\test\\resources\\idl\\Test.idl");
        IDLClass idlClass = idlReader.getClass("ExtendClassTest");
        Assert.assertEquals("NormalClassTest", idlClass.extendClass);
    }


}

