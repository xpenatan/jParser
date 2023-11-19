package idl.helper;

import idl.IDLBase;

public class IDLFloatArray extends IDLBase {

    public static IDLFloatArray tmp1_1 = new IDLFloatArray(1);
    public static IDLFloatArray tmp1_2 = new IDLFloatArray(1);

    public static IDLFloatArray tmp2_1 = new IDLFloatArray(2);
    public static IDLFloatArray tmp2_2 = new IDLFloatArray(2);

    public static IDLFloatArray tmp3_1 = new IDLFloatArray(3);
    public static IDLFloatArray tmp3_2 = new IDLFloatArray(3);

    public static IDLFloatArray tmp4_1 = new IDLFloatArray(4);
    public static IDLFloatArray tmp4_2 = new IDLFloatArray(4);

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

    public IDLFloatArray(int size) {
    }

    public void copy(float [] array) {
        int length = array.length;
        resize(length);
        for(int i = 0; i < length; i++) {
            float value = array[i];
            setValue(i, value);
        }
    }

    public native void setValue(int index, float value);
    public native void resize(int size);
    public native float getValue(int index);
    public native long getPointer();
    public native int getSize();
}