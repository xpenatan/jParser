package com.github.xpenatan.jparser.example.app;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.utils.ScreenUtils;
import lib.emscripten.TestLibLoader;
import lib.test.ArrayArgumentTest;
import lib.test.ArrayClass;
import lib.test.Child1;
import lib.test.Child2;
import lib.test.EnumClass;
import lib.test.EnumClass_EnumWithinClass;
import lib.test.Inner;
import lib.test.ObjectFactory;
import lib.test.Parent;
import lib.test.ReceiveArrays;
import lib.test.RefUser;
import lib.test.StoreArray;
import lib.test.StringUser;
import lib.test.StructInArray;
import lib.test.TypeTestClass;
import lib.test.VoidPointerUser;
import lib.test.idl.helper.IDLByteArray;
import lib.test.idl.helper.IDLIntArray;
import static lib.test.AnEnum.enum_value1;
import static lib.test.AnEnum.enum_value2;
import static lib.test.EnumNamespace_EnumInNamespace.e_namespace_val;

public class AppTestLib extends ApplicationAdapter {
    private boolean init = false;

    @Override
    public void create() {
        TestLibLoader.init(new Runnable() {
            @Override
            public void run() {
                init = true;
            }
        });
    }

    @Override
    public void render() {
        ScreenUtils.clear(0.4f, 0.4f, 0.4f, 1);
        if(init) {
            init = false;
            initLib();
        }
    }


    private void initLib() {
        test();
    }

    private void test() {
        Console console = new Console();

        // Part 1

        var sme = new Parent(42);
        sme.mulVal(2);
        console.log("*");
        console.log(sme.getVal());
        console.log(sme.getAsConst());
        console.log(sme.voidStar(sme.getCPointer()));
        console.log(sme.get_immutableAttr());
        console.log(sme.get_immutableAttr());

        console.log(sme.get_attr());
        sme.set_attr(9);
        console.log(sme.get_attr());
        sme.set_attr(10);
        console.log(sme.get_attr());

        console.log(sme.getBoolean());

        console.log("c1");
//
        var c1 = new Child1();
        console.log(c1.getVal());
        c1.mulVal(2);
        console.log(c1.getVal());
        console.log(c1.getValSqr());
        console.log(c1.getValSqr(3));
//        console.log(c1.getValTimes()); // default argument should be 1
//        console.log(c1.getValTimes(2));
        console.log(sme.getBoolean());
        c1.parentFunc(90);

        console.log("c1 v2");
//
        c1 = new Child1(8); // now with a parameter, we should handle the overloading automatically and properly and use constructor #2
        console.log(c1.getVal());
        c1.mulVal(2);
        console.log(c1.getVal());
        console.log(c1.getValSqr());
        console.log(c1.getValSqr(3));
        console.log(sme.getBoolean());

        console.log("c2");

        int succeeded = 0;

        var c2 = new Child2();
        console.log(c2.getVal());
        c2.mulVal(2);
        console.log(c2.getVal());
        console.log(c2.getValCube());
        try {
            succeeded = 0;
            c2.getValCube(); // sanity
            succeeded = 1;
        } catch(Throwable t) {}
        console.log(succeeded);

        Child2.printStatic(42); // static calls go through the prototype

        // virtual function
//        c2.virtualFunc();
//        Child2.runVirtualFunc(c2);
//        c2.virtualFunc2();

        // extend a class from JS
//        var c3 = new TheModule.Child2JS;
//
//        c3.virtualFunc = function() {
//            console.log('*js virtualf replacement*');
//        };
//        c3.virtualFunc2 = function() {
//            console.log('*js virtualf2 replacement*');
//        };
//        c3.virtualFunc3 = function(x) {
//            console.log('*js virtualf3 replacement ' + x + '*');
//        };
//
//        c3.virtualFunc();
//        TheModule.Child2.prototype.runVirtualFunc(c3);
//        c3.virtualFunc2();
//        c3.virtualFunc3(123); // this one is not replaced!
//        try {
//            c3.virtualFunc4(123);
//        } catch(e) {
//            console.log('caught: ' + e);
//        }
//
        // Test virtual method dispatch from c++
//        Child2.runVirtualFunc3(c3, 43);
//
//        c2.virtualFunc(); // original should remain the same
//        Child2.runVirtualFunc(c2);
//        c2.virtualFunc2();
//        console.log("*ok*");

        // Part 2

        var suser = new StringUser("hello", 43);
        suser.Print(41, "world");
        suser.PrintFloat(12.3456f);
//        console.log(suser.returnAString());

        var bv = new RefUser(10);
        var bv2 = new RefUser(11);
        console.log(bv2.getValue(bv));

        console.log(bv2.getMe().getClass());
        console.log(bv2.getMe().getValue(bv));
        console.log(bv2.getMe().getValue(bv2));

        console.log(bv2.getCopy().getClass());
        console.log(bv2.getCopy().getValue(bv));
        console.log(bv2.getCopy().getValue(bv2));

        bv2.getAnother().PrintFloat(21.12f);

        console.log(new Inner().get());
//        console.log('getAsArray: ' + new Inner().getAsArray(12));
//        new Inner().mul(2);
//        new Inner().incInPlace(new Inner());

        console.log(enum_value1);
        console.log(enum_value2);

        // Enums from classes are accessed via the class.
        var enumClassInstance = new EnumClass();
        console.log(enumClassInstance.GetEnum());
        console.log(EnumClass_EnumWithinClass.e_val);
//        console.log([enumClassInstance.GetEnum(), EnumClass.e_val].join(','));

        // Enums from namespaces are accessed via the top-level module, as with classes defined
        // in namespaces, see `Inner` above.
        console.log(e_namespace_val);

        var typeTester = new TypeTestClass();

        console.log("return char " + (typeTester.ReturnCharMethod() & 255));
        typeTester.AcceptCharMethod((byte)((2<<6)-1));
        typeTester.AcceptCharMethod((byte)-1);

//        console.log("return unsigned char " + (typeTester.ReturnUnsignedCharMethod() & 255));
//        typeTester.AcceptUnsignedCharMethod((2<<7)-1);

        console.log("return unsigned short " + (typeTester.ReturnUnsignedShortMethod() & 65535));
        typeTester.AcceptUnsignedShortMethod((short)((2<<15)-1));

        console.log("return unsigned long " + (typeTester.ReturnUnsignedLongMethod() | 0));
        typeTester.AcceptUnsignedLongMethod((2<<31)-1);
        var voidPointerUser = new VoidPointerUser();

        voidPointerUser.SetVoidPointer(3);
        console.log("void * " + voidPointerUser.GetVoidPointer());

        // Array tests

        var arrayClass = new ArrayClass();
//        console.log("int_array[0] == " + arrayClass.get_int_array(0));
//        console.log("int_array[7] == " + arrayClass.get_int_array(7));
//        arrayClass.set_int_array(0, 42);
//        arrayClass.set_int_array(7, 43);
//        console.log("int_array[0] == " + arrayClass.get_int_array(0));
//        console.log("int_array[7] == " + arrayClass.get_int_array(7));

//        try {
//            arrayClass.set_int_array(-1, 42);
//        } catch (Throwable e) {
//            console.log("idx -1: " + e);
//        }

//        try {
//            arrayClass.set_int_array(8, 42);
//        } catch (Throwable e) {
//            console.log("idx 8: " + e);
//        }
//
//        console.log("struct_array[0].attr1 == " + arrayClass.get_struct_array(0).get_attr1());
//        console.log("struct_array[0].attr2 == " + arrayClass.get_struct_array(0).get_attr2());
//        console.log("struct_array[7].attr1 == " + arrayClass.get_struct_array(7).get_attr1());
//        console.log("struct_array[7].attr2 == " + arrayClass.get_struct_array(7).get_attr2());

        // Verify that bounds checking is *not* enabled when not asked for.
        // This actually causes an illegal memory access, but as it's only a read, and the return
        // value is not used, it shouldn't cause any problems in practice.
//        arrayClass.get_struct_array(8);
//
        var struct = new StructInArray(13, 17);
//        arrayClass.set_struct_array(0, struct);
        struct = new StructInArray(14, 18);
//        arrayClass.set_struct_array(7, struct);

//        console.log("struct_array[0].attr1 == " + arrayClass.get_struct_array(0).get_attr1());
//        console.log("struct_array[0].attr2 == " + arrayClass.get_struct_array(0).get_attr2());
//        console.log("struct_array[7].attr1 == " + arrayClass.get_struct_array(7).get_attr1());
//        console.log("struct_array[7].attr2 == " + arrayClass.get_struct_array(7).get_attr2());
//
        struct = new StructInArray(100, 101);
//        arrayClass.set_struct_ptr_array(0, struct);
//        console.log("struct_ptr_array[0]->attr1 == " + arrayClass.get_struct_ptr_array(0).get_attr1());
//        console.log("struct_ptr_array[0]->attr2 == " + arrayClass.get_struct_ptr_array(0).get_attr2());

        // receiving arrays

        var receiver = new ReceiveArrays();
//        receiver.giveMeArrays([0.5, 0.25, 0.01, -20.42], [1, 4, 9, 10], 4);

        // Test IDL_CHECKS=ALL

//        try {
//            p = new Parent(NaN); // Expects an integer
//        } catch (e) {}

        try {
            Parent p = new Parent(42);
            p.voidStar(1234); // Expects a wrapped pointer
        } catch (Throwable e) {}

        try {
            StringUser s = new StringUser("abc", 1);
            s.Print(123, null); // Expects a string or a wrapped pointer
        } catch (Throwable e) {}

        // Returned pointers (issue 14745)

        var factory = new ObjectFactory();
        var objectProvider = factory.getProvider();
        var smallObject = objectProvider.getObject();

        // This will print 123 if we managed to access the object, which means that integers
        // were correctly typecast to ObjectProvider pointer and SmallObject pointer.
        console.log(smallObject.getID(123));

        factory.dispose();

        // end of issue 14745

        // octet[] to char* (issue 14827)

        var arrayTestObj = new ArrayArgumentTest();
        var bufferAddr = new IDLByteArray(35);
//        TheModule.stringToUTF8('I should match the member variable', bufferAddr, 35);

        var arrayTestResult = arrayTestObj.byteArrayTest(bufferAddr);
        var arrayDomStringResult = arrayTestObj.domStringTest("I should match the member variable");
        console.log(arrayTestResult);
        console.log(arrayDomStringResult);

        arrayTestObj.dispose();

        bufferAddr.dispose();
        // end of issue 14827

        // Check for overflowing the stack

//        var before = Date.now();
//
//        for (var i = 0; i < 1000000; i++) {
//            var temp = new TheModule.StringUser('abc', 1);
//            TheModule.destroy(temp);
//            if (Date.now() - before >= 1000) break;
//        }

        boolean isMemoryGrowthAllowed = true;
        if(isMemoryGrowthAllowed) {
            // Check for HEAP reallocation when using large arrays
            var numArrayEntries = 100000;
            var intArray = new IDLIntArray(numArrayEntries);
            for (var i = 0; i < numArrayEntries; i++) {
                intArray.setValue(i, i);
            }

//            var startHeapLength = TheModule['HEAP8'].length;
            int offset;
            var storeArray = new StoreArray();
            storeArray.setArray(intArray);
            // Add more data until the heap is reallocated
//            while (TheModule['HEAP8'].length === startHeapLength) {
//                intArray = intArray.concat(intArray);
//                storeArray.setArray(intArray);
//            }
//
//            // Make sure the array was copied to the newly allocated HEAP
//            var numCopiedEntries = 0;
//            for (var i = 0; i < intArray.length; i++) {
//                if (storeArray.getArrayValue(i) !== intArray[i]) {
//                    break;
//                }
//                numCopiedEntries += 1;
//            }
//
//            if (intArray.length !== numCopiedEntries) {
//                console.log('ERROR: An array was not copied to HEAP32 after memory reallocation');
//            }
        }
        console.log("\ndone.");
    }

    static class Console {
        public void log(Object text) {
            System.out.println(text);
        }
        public void log(Class text) {
            System.out.println(text);
        }
        public void log(int text) {
            System.out.println(text);
        }
        public void log(float text) {
            System.out.println(text);
        }
        public void log(boolean text) {
            System.out.println(text);
        }
        public void log(String text) {
            System.out.println(text);
        }
    }
}