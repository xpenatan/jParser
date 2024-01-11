package idl.helper;

import idl.IDLBase;

public class IDLBoolArray extends IDLBase {

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