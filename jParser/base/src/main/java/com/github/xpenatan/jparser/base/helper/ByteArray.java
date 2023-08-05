package com.github.xpenatan.jparser.base.helper;

import com.github.xpenatan.jparser.base.IDLBase;

public class ByteArray extends IDLBase {
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
}