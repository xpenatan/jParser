package idl.helper;

import idl.IDLBase;

public class ByteArray extends IDLBase {

    public ByteArray(int size) {
    }

    public void copy(byte [] array) {
        int length = array.length;
        resize(length);
        for(int i = 0; i < length; i++) {
            byte value = array[i];
            setValue(i, value);
        }
    }

    public native void setValue(int index, byte value);
    public native void resize(int size);
    public native byte getValue(int index);
    public native long getPointer();
    public native int getSize();
}