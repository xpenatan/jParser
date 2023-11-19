package idl.helper;

import idl.IDLBase;

public class IDLIntArray extends IDLBase {

    public static IDLIntArray tmp1_1 = new IDLIntArray(1);
    public static IDLIntArray tmp1_2 = new IDLIntArray(1);

    public static IDLIntArray tmp2_1 = new IDLIntArray(2);
    public static IDLIntArray tmp2_2 = new IDLIntArray(2);

    public static IDLIntArray tmp3_1 = new IDLIntArray(3);
    public static IDLIntArray tmp3_2 = new IDLIntArray(3);

    public static IDLIntArray tmp4_1 = new IDLIntArray(4);
    public static IDLIntArray tmp4_2 = new IDLIntArray(4);

    public static void disposeTEMP() {
        tmp1_1.dispose();
        tmp1_2.dispose();
        tmp2_1.dispose();
        tmp2_2.dispose();
        tmp3_1.dispose();
        tmp3_2.dispose();
        tmp4_1.dispose();
        tmp4_2.dispose();
    }

    public IDLIntArray(int size) {
    }

    public void copy(int[] array) {
        int length = array.length;
        resize(length);
        for(int i = 0; i < length; i++) {
            int value = array[i];
            setValue(i, value);
        }
    }

    public native void setValue(int index, int value);
    public native void resize(int size);
    public native int getValue(int index);
    public native long getPointer();
    public native int getSize();
}