package idl.helper;

public class IDLBoolArray extends IDLArray {

    public static final IDLBoolArray NULL = native_new();

    /**
     * @return An empty instance without a native address
     */
    public static IDLBoolArray native_new() {
        return new IDLBoolArray((byte) 1, (char) 1);
    }

    protected IDLBoolArray(byte b, char c) {
    }

    protected IDLBoolArray() {}

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