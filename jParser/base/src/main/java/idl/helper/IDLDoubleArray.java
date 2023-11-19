package idl.helper;

import idl.IDLBase;

public class IDLDoubleArray extends IDLBase {

    public static IDLDoubleArray tmp1_1 = new IDLDoubleArray(1);
    public static IDLDoubleArray tmp1_2 = new IDLDoubleArray(1);

    public static IDLDoubleArray tmp2_1 = new IDLDoubleArray(2);
    public static IDLDoubleArray tmp2_2 = new IDLDoubleArray(2);

    public static IDLDoubleArray tmp3_1 = new IDLDoubleArray(3);
    public static IDLDoubleArray tmp3_2 = new IDLDoubleArray(3);

    public static IDLDoubleArray tmp4_1 = new IDLDoubleArray(4);
    public static IDLDoubleArray tmp4_2 = new IDLDoubleArray(4);

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

    public IDLDoubleArray(int size) {
    }

    public void copy(double [] array) {
        int length = array.length;
        resize(length);
        for(int i = 0; i < length; i++) {
            double value = array[i];
            setValue(i, value);
        }
    }

    public native void setValue(int index, double value);
    public native void resize(int size);
    public native double getValue(int index);
    public native long getPointer();
    public native int getSize();
}