package com.github.xpenatan.jparser.base.helper;

import com.github.xpenatan.jparser.base.IDLBase;

public class IntArray extends IDLBase {
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
}