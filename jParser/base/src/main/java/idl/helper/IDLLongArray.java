package idl.helper;

import idl.IDLBase;

public class IDLLongArray extends IDLBase {

    public IDLLongArray(int size) {
    }

    public void copy(long[] array) {
        int length = array.length;
        resize(length);
        for(int i = 0; i < length; i++) {
            long value = array[i];
            setValue(i, value);
        }
    }

    public native void setValue(int index, long value);
    public native void resize(int size);
    public native long getValue(int index);
    public native long getPointer();
    public native int getSize();
}