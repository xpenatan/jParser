package com.github.xpenatan.jParser.example.app.tests;

import com.github.xpenatan.jParser.example.app.CodeTest;
import com.github.xpenatan.jParser.example.testlib.TestBufferManualClass;
import com.github.xpenatan.jparser.runtime.helper.NativeIntArray;
import com.github.xpenatan.jparser.runtime.helper.NativeUtils;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

public class BufferTest implements CodeTest {
    private static boolean testManualByteBufferPositionInvariant() {
        TestBufferManualClass test = new TestBufferManualClass();
        try {
            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(3);
            byteBuffer.put(0, (byte)1);
            byteBuffer.put(1, (byte)2);
            byteBuffer.put(2, (byte)3);

            int oldPos = 3;
            int oldLimit = byteBuffer.limit();
            byteBuffer.position(oldPos);

            test.updateByteBuffer(byteBuffer, 3, (byte)2);

            if(byteBuffer.position() != oldPos || byteBuffer.limit() != oldLimit) {
                throw new RuntimeException("ByteBuffer state changed after updateByteBuffer");
            }

            byte v0 = byteBuffer.get(0);
            byte v1 = byteBuffer.get(1);
            byte v2 = byteBuffer.get(2);
            if(!(v0 == 0x02 && v1 == 0x02 && v2 == 0x02)) {
                throw new RuntimeException("ByteBuffer write must use base address independent of position");
            }
        }
        catch(Throwable e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private static boolean testBufferManualClass() {
        {
            TestBufferManualClass test = new TestBufferManualClass();
            try {
                ByteBuffer byteBuffer = ByteBuffer.allocateDirect(3);
                byteBuffer.put(0, (byte)1);
                byteBuffer.put(1, (byte)2);
                byteBuffer.put(2, (byte)3);
                test.updateByteBuffer(byteBuffer, 3, (byte)2);
                byte v0 = byteBuffer.get(0);
                byte v1 = byteBuffer.get(1);
                byte v2 = byteBuffer.get(2);
                if(!(v0 == 0x02 && v1 == 0x02 && v2 == 0x02)) {
                    System.out.println("v0: " + v0);
                    System.out.println("v1: " + v1);
                    System.out.println("v2: " + v2);
                    throw new RuntimeException("v0 == 0x02 && v1 == 0x02 && v2 == 0x02");
                }
            } catch(Throwable e) {
                e.printStackTrace();
                return false;
            }
        }

        return true;
    }

    private static boolean testCopyToByteBufferPositionInvariant() {
        NativeIntArray test = null;
        try {
            test = new NativeIntArray(3);
            test.setValue(0, 10);
            test.setValue(1, 20);
            test.setValue(2, 30);

            int sizeInBytes = 3 * Integer.BYTES;
            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(5 * Integer.BYTES);
            byteBuffer.order(ByteOrder.LITTLE_ENDIAN);

            IntBuffer allInts = byteBuffer.duplicate().order(ByteOrder.LITTLE_ENDIAN).asIntBuffer();
            allInts.put(0, -1);
            allInts.put(1, -1);
            allInts.put(2, -1);
            allInts.put(3, -1);
            allInts.put(4, -1);

            int oldPos = Integer.BYTES;
            int oldLimit = 4 * Integer.BYTES;
            byteBuffer.position(oldPos);
            byteBuffer.limit(oldLimit);

            NativeUtils.copyToByteBuffer(test, byteBuffer, Integer.BYTES, sizeInBytes);

            if(byteBuffer.position() != oldPos || byteBuffer.limit() != oldLimit) {
                throw new RuntimeException("ByteBuffer state changed after copyToByteBuffer");
            }

            int x0 = allInts.get(0);
            int x1 = allInts.get(1);
            int x2 = allInts.get(2);
            int x3 = allInts.get(3);
            int x4 = allInts.get(4);
            if(!(x0 == -1 && x1 == 10 && x2 == 20 && x3 == 30 && x4 == -1)) {
                throw new RuntimeException("copyToByteBuffer must honor explicit offset and ignore destination position");
            }
        }
        catch(Throwable e) {
            e.printStackTrace();
            return false;
        }
        finally {
            if(test != null) {
                test.dispose();
            }
        }
        return true;
    }

    private static boolean testArrayToByteBuffer() {
        {
            NativeIntArray test = null;
            try {
                test = new NativeIntArray(3);
                {
                    test.setValue(0, 10);
                    test.setValue(1, 20);
                    test.setValue(2, 30);
                    int sizeInBytes = 3 * Integer.BYTES;
                    ByteBuffer byteBuffer = ByteBuffer.allocateDirect(sizeInBytes);
                    byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
                    IntBuffer intBuffer = byteBuffer.asIntBuffer();
                    NativeUtils.copyToByteBuffer(test, byteBuffer, 0, sizeInBytes);
                    int x = intBuffer.get(0);
                    int y = intBuffer.get(1);
                    int z = intBuffer.get(2);

                    if(!(x == 10 && y == 20 && z == 30)) {
                        throw new RuntimeException();
                    }
                }
            } catch(Throwable e) {
                e.printStackTrace();
                return false;
            } finally {
                test.dispose();
            }
        }
        return true;
    }

    @Override
    public boolean test() {
        return testBufferManualClass()
                && testManualByteBufferPositionInvariant()
                && testArrayToByteBuffer()
                && testCopyToByteBufferPositionInvariant();
    }
}
