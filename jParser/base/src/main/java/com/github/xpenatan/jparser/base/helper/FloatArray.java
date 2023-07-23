package com.github.xpenatan.jparser.base.helper;

import com.github.xpenatan.jparser.base.IDLBase;

public class FloatArray extends IDLBase {
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