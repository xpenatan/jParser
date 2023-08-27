package idl.helper;

import idl.IDLBase;

public class FloatArray extends IDLBase {

    public FloatArray(int size) {
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
}