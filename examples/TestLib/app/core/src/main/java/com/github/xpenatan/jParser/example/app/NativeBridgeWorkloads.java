package com.github.xpenatan.jParser.example.app;

import com.github.xpenatan.jParser.example.testlib.TestAttributeClass;
import com.github.xpenatan.jParser.example.testlib.TestBufferManualClass;
import com.github.xpenatan.jParser.example.testlib.TestEnumLib;
import com.github.xpenatan.jParser.example.testlib.TestMethodClass;
import com.github.xpenatan.jParser.example.testlib.TestObjectClass;
import com.github.xpenatan.jparser.idl.helper.IDLFloat;
import com.github.xpenatan.jparser.idl.helper.IDLFloatArray;
import com.github.xpenatan.jparser.idl.helper.IDLInt;
import com.github.xpenatan.jparser.idl.helper.IDLIntArray;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Shared native bridge workloads used by both throughput and FPS benchmarks.
 */
public class NativeBridgeWorkloads {

    @FunctionalInterface
    public interface IterationWorkload {
        void run(long iterations);
    }

    public static class Scenario {
        public final String label;
        private final IterationWorkload workload;

        public Scenario(String label, IterationWorkload workload) {
            this.label = label;
            this.workload = workload;
        }

        public void run(long iterations) {
            workload.run(iterations);
        }
    }

    private final TestMethodClass methodObj;
    private final TestObjectClass objectA;
    private final TestObjectClass objectB;
    private final TestAttributeClass attrObj;
    private final TestBufferManualClass bufferObj;
    private final IDLIntArray intArray;
    private final IDLFloatArray floatArray;
    private final IDLInt idlInt;
    private final IDLFloat idlFloat;
    private final ByteBuffer byteBuffer;

    private final Scenario[] scenarios;

    private volatile int intSink;
    private volatile float floatSink;

    public NativeBridgeWorkloads() {
        methodObj = new TestMethodClass();
        objectA = new TestObjectClass();
        objectB = new TestObjectClass();
        attrObj = new TestAttributeClass();
        bufferObj = new TestBufferManualClass();
        intArray = new IDLIntArray(64);
        floatArray = new IDLFloatArray(64);
        idlInt = new IDLInt();
        idlFloat = new IDLFloat();

        byteBuffer = ByteBuffer.allocateDirect(256);
        byteBuffer.order(ByteOrder.nativeOrder());

        objectA.set_intValue01(42);
        objectA.set_floatValue01(3.14f);
        objectB.set_intValue01(99);
        objectB.set_floatValue01(2.71f);
        methodObj.setMethod05("hello");

        for(int i = 0; i < 64; i++) {
            intArray.setValue(i, i);
            floatArray.setValue(i, i);
        }
        for(int i = 0; i < 256; i++) {
            byteBuffer.put(i, (byte)i);
        }

        scenarios = new Scenario[] {
                new Scenario("void(int)", this::setInt),
                new Scenario("void(float, bool)", this::setFloatBool),
                new Scenario("void(int,int,float,float,bool)", this::setManyPrimitives),
                new Scenario("int getter", this::getInt),
                new Scenario("float getter", this::getFloat),
                new Scenario("void(String)", this::setString),
                new Scenario("void(TestEnumLib)", this::setEnum),
                new Scenario("String getter", this::getString),
                new Scenario("void(Obj*,Obj*,Obj&,Obj&)", this::objectPassing),
                new Scenario("Object* getter", this::objectReturn),
                new Scenario("attribute set int", this::attrSetInt),
                new Scenario("attribute get int", this::attrGetInt),
                new Scenario("attribute set float", this::attrSetFloat),
                new Scenario("attribute get float", this::attrGetFloat),
                new Scenario("IDLIntArray set+get", this::idlIntArray),
                new Scenario("IDLFloatArray set+get", this::idlFloatArray),
                new Scenario("IDLInt set+getValue", this::idlIntValue),
                new Scenario("IDLFloat set+getValue", this::idlFloatValue),
                new Scenario("ByteBuffer update (256B)", this::byteBufferUpdate),
        };
    }

    public Scenario[] getScenarios() {
        return scenarios;
    }

    public void dispose() {
        idlFloat.dispose();
        idlInt.dispose();
        floatArray.dispose();
        intArray.dispose();
        attrObj.dispose();
        objectB.dispose();
        objectA.dispose();
        methodObj.dispose();
    }

    private void setInt(long iterations) {
        for(long i = 0; i < iterations; i++) {
            methodObj.setMethod01((int)i);
        }
    }

    private void setFloatBool(long iterations) {
        for(long i = 0; i < iterations; i++) {
            methodObj.setMethod02(1.5f, true);
        }
    }

    private void setManyPrimitives(long iterations) {
        for(long i = 0; i < iterations; i++) {
            methodObj.setMethod03(1, 2, 3.0f, 4.0f, true);
        }
    }

    private void getInt(long iterations) {
        int sink = 0;
        for(long i = 0; i < iterations; i++) {
            sink += methodObj.getIntValue01();
        }
        intSink = sink;
    }

    private void getFloat(long iterations) {
        float sink = 0f;
        for(long i = 0; i < iterations; i++) {
            sink += methodObj.getFloatValue01();
        }
        floatSink = sink;
    }

    private void setString(long iterations) {
        for(long i = 0; i < iterations; i++) {
            methodObj.setMethod05("benchmark");
        }
    }

    private void setEnum(long iterations) {
        for(long i = 0; i < iterations; i++) {
            methodObj.setMethod10(TestEnumLib.TEST_SECOND);
        }
    }

    private void getString(long iterations) {
        for(long i = 0; i < iterations; i++) {
            methodObj.getStrValue01().data();
        }
    }

    private void objectPassing(long iterations) {
        for(long i = 0; i < iterations; i++) {
            methodObj.setMethod06(objectA, objectB, objectA, objectB);
        }
    }

    private void objectReturn(long iterations) {
        for(long i = 0; i < iterations; i++) {
            methodObj.getPointerObject02();
        }
    }

    private void attrSetInt(long iterations) {
        for(long i = 0; i < iterations; i++) {
            attrObj.set_intValue01((int)i);
        }
    }

    private void attrGetInt(long iterations) {
        int sink = 0;
        for(long i = 0; i < iterations; i++) {
            sink += attrObj.get_intValue01();
        }
        intSink = sink;
    }

    private void attrSetFloat(long iterations) {
        for(long i = 0; i < iterations; i++) {
            attrObj.set_floatValue01((float)i);
        }
    }

    private void attrGetFloat(long iterations) {
        float sink = 0f;
        for(long i = 0; i < iterations; i++) {
            sink += attrObj.get_floatValue01();
        }
        floatSink = sink;
    }

    private void idlIntArray(long iterations) {
        int sink = 0;
        for(long i = 0; i < iterations; i++) {
            int idx = (int)(i & 63L);
            intArray.setValue(idx, (int)i);
            sink += intArray.getValue(idx);
        }
        intSink = sink;
    }

    private void idlFloatArray(long iterations) {
        float sink = 0f;
        for(long i = 0; i < iterations; i++) {
            int idx = (int)(i & 63L);
            floatArray.setValue(idx, i);
            sink += floatArray.getValue(idx);
        }
        floatSink = sink;
    }

    private void idlIntValue(long iterations) {
        int sink = 0;
        for(long i = 0; i < iterations; i++) {
            idlInt.set((int)i);
            sink += idlInt.getValue();
        }
        intSink = sink;
    }

    private void idlFloatValue(long iterations) {
        float sink = 0f;
        for(long i = 0; i < iterations; i++) {
            idlFloat.set(i);
            sink += idlFloat.getValue();
        }
        floatSink = sink;
    }

    private void byteBufferUpdate(long iterations) {
        for(long i = 0; i < iterations; i++) {
            bufferObj.updateByteBuffer(byteBuffer, 256, (byte)0x42);
        }
    }
}
