package com.github.xpenatan.jParser.example.app.tests;

import com.github.xpenatan.jParser.example.app.CodeTest;
import com.github.xpenatan.jParser.example.testlib.TestBufferManualClass;
import com.github.xpenatan.jparser.idl.helper.IDLIntArray;
import com.github.xpenatan.jparser.idl.helper.IDLUtils;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

public class BufferTest implements CodeTest {
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

    private static boolean testArrayToByteBuffer() {
        {
            IDLIntArray test = null;
            try {
                test = new IDLIntArray(3);
                {
                    test.setValue(0, 10);
                    test.setValue(1, 20);
                    test.setValue(2, 30);
                    int sizeInBytes = 3 * Integer.BYTES;
                    ByteBuffer byteBuffer = ByteBuffer.allocateDirect(sizeInBytes);
                    byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
                    IntBuffer intBuffer = byteBuffer.asIntBuffer();
                    IDLUtils.copyToByteBuffer(test, byteBuffer, 0, sizeInBytes);
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
        return testBufferManualClass() && testArrayToByteBuffer();
    }
}