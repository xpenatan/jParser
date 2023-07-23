package com.github.xpenatan.jparser.base.helper;

import com.github.xpenatan.jparser.base.IDLBase;

public class CharArray extends IDLBase {
    public void copy(char [] array) {
        int length = array.length;
        resize(length);
        for(int i = 0; i < length; i++) {
            char value = array[i];
            setValue(i, value);
        }
    }

    public native void setValue(int index, char value);
    public native void resize(int size);
    public native int getSize();
}