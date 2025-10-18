package idl.helper;

public class IDLArrayInt extends IDLArray {

    public static final IDLArrayInt NULL = native_new();

    /**
     * @return An empty instance without a native address
     */
    public static IDLArrayInt native_new() {
        return new IDLArrayInt((byte) 1, (char) 1);
    }

    protected IDLArrayInt() {}

    protected IDLArrayInt(byte b, char c) {
    }

    public IDLArrayInt(int size) {
    }

    public void copy(int[] array) {
        int length = array.length;
        resize(length);
        for(int i = 0; i < length; i++) {
            int value = array[i];
            setValue(i, value);
        }
    }

    public native void setValue(int index, int value);
    public native int getValue(int index);
}