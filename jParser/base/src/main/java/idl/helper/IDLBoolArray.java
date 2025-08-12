package idl.helper;

public class IDLBoolArray extends IDLArrayBase {

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
    public native boolean getValue(int index);
}