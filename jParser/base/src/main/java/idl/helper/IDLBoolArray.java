package idl.helper;

import idl.IDLBase;

public class IDLBoolArray extends IDLBase {

    public static IDLBoolArray tmp1_1 = new IDLBoolArray(1);
    public static IDLBoolArray tmp1_2 = new IDLBoolArray(1);

    public static IDLBoolArray tmp2_1 = new IDLBoolArray(2);
    public static IDLBoolArray tmp2_2 = new IDLBoolArray(2);

    public static IDLBoolArray tmp3_1 = new IDLBoolArray(3);
    public static IDLBoolArray tmp3_2 = new IDLBoolArray(3);

    public static IDLBoolArray tmp4_1 = new IDLBoolArray(4);
    public static IDLBoolArray tmp4_2 = new IDLBoolArray(4);

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

    public IDLBoolArray(int size) {
    }

    public void copy(boolean [] array) {
        int length = array.length;
        resize(length);
        for(int i = 0; i < length; i++) {
            boolean value = array[i];
            setValue(i, value);
        }
    }

    public native void setValue(int index, boolean value);
    public native void resize(int size);
    public native boolean getValue(int index);
    public native long getPointer();
    public native int getSize();
}