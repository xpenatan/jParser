package idl.helper;

public class IDLArrayLong extends IDLArray {

    public static final IDLArrayLong NULL = native_new();

    /**
     * @return An empty instance without a native address
     */
    public static IDLArrayLong native_new() {
        return new IDLArrayLong((byte) 1, (char) 1);
    }

    protected IDLArrayLong(byte b, char c) {}

    protected IDLArrayLong() {}

    public IDLArrayLong(int size) {
    }

    public void copy(long[] array) {
        int length = array.length;
        resize(length);
        for(int i = 0; i < length; i++) {
            long value = array[i];
            setValue(i, value);
        }
    }

    public native void setValue(int index, long value);
    public native long getValue(int index);
}