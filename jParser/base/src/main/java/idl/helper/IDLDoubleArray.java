package idl.helper;

import idl.IDLBase;

public class IDLDoubleArray extends IDLBase {

    public static IDLDoubleArray TMP1_1 = new IDLDoubleArray(1);
    public static IDLDoubleArray TMP1_2 = new IDLDoubleArray(1);

    public static IDLDoubleArray TMP2_1 = new IDLDoubleArray(2);
    public static IDLDoubleArray TMP2_2 = new IDLDoubleArray(2);

    public static IDLDoubleArray TMP3_1 = new IDLDoubleArray(3);
    public static IDLDoubleArray TMP3_2 = new IDLDoubleArray(3);

    public static IDLDoubleArray TMP4_1 = new IDLDoubleArray(4);
    public static IDLDoubleArray TMP4_2 = new IDLDoubleArray(4);

    public static void disposeTEMP() {
        TMP1_1.dispose();
        TMP1_2.dispose();
        TMP2_1.dispose();
        TMP2_2.dispose();
        TMP3_1.dispose();
        TMP3_2.dispose();
        TMP4_1.dispose();
        TMP4_2.dispose();
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