package idl.helper;

import idl.IDLBase;

public class DoubleArray extends IDLBase {

    public DoubleArray(int size) {
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
}