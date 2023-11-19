package idl.helper;

import idl.IDLBase;

public class IDLBoolArray extends IDLBase {

    public static IDLBoolArray TMP1_1 = new IDLBoolArray(1);
    public static IDLBoolArray TMP1_2 = new IDLBoolArray(1);

    public static IDLBoolArray TMP2_1 = new IDLBoolArray(2);
    public static IDLBoolArray TMP2_2 = new IDLBoolArray(2);

    public static IDLBoolArray TMP3_1 = new IDLBoolArray(3);
    public static IDLBoolArray TMP3_2 = new IDLBoolArray(3);

    public static IDLBoolArray TMP4_1 = new IDLBoolArray(4);
    public static IDLBoolArray TMP4_2 = new IDLBoolArray(4);

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