package idl.helper;

import idl.IDLBase;

public class IDLIntArray extends IDLBase {

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