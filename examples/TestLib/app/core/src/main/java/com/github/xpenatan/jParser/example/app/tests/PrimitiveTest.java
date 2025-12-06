package com.github.xpenatan.jParser.example.app.tests;

import com.github.xpenatan.jParser.example.app.CodeTest;
import com.github.xpenatan.jparser.idl.helper.IDLInt;
import com.github.xpenatan.jparser.idl.helper.IDLInt4;
import com.github.xpenatan.jparser.idl.helper.IDLIntArray;

public class PrimitiveTest implements CodeTest {
    private static boolean testPrimitivePointers() {
        {
            IDLInt test = null;
            try {
                test = new IDLInt();
                test.set(10);
                int value = test.getValue();
                if(!(value == 10)) {
                    test.dispose();
                    throw new RuntimeException();
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

    private static boolean testPrimitiveArray() {
        {
            IDLIntArray test = null;
            try {
                test = new IDLIntArray(2);
                {
                    test.setValue(0, 10);
                    test.setValue(1, 20);
                    int value01 = test.getValue(0);
                    int value02 = test.getValue(1);
                    if(!(value01 == 10 && value02 == 20)) {
                        throw new RuntimeException();
                    }
                }
                {
                    test.resize(3);
                    test.setValue(0, 9);
                    test.setValue(1, 8);
                    test.setValue(2, 7);
                    int value01 = test.getValue(0);
                    int value02 = test.getValue(1);
                    int value03 = test.getValue(2);
                    if(!(value01 == 9 && value02 == 8 && value03 == 7)) {
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
        {
            IDLInt4 test = null;
            try {
                test = new IDLInt4();

                test.set(1,2,3, 4);
                int x = test.getX();
                int y = test.getY();
                int z = test.getZ();
                int w = test.getW();
                if(!(x == 1 && y == 2 && z == 3 && w == 4)) {
                    throw new RuntimeException();
                }

                test.setValue(0, 11);
                test.setValue(1, 12);
                test.setValue(2, 13);
                test.setValue(3, 14);
                int x2 = test.getValue(0);
                int y2 = test.getValue(1);
                int z2 = test.getValue(2);
                int w2 = test.getValue(3);
                if(!(x2 == 11 && y2 == 12 && z2 == 13 && w2 == 14)) {
                    throw new RuntimeException();
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
        return testPrimitivePointers() && testPrimitiveArray();
    }
}