package idl.helper;

public class IDLLongArray extends IDLArrayBase {

    public static final IDLLongArray NULL = native_new();

    /**
     * @return An empty instance without a native address
     */
    public static IDLLongArray native_new() {
        return new IDLLongArray((byte) 1, (char) 1);
    }

    protected IDLLongArray(byte b, char c) {}

    public IDLLongArray(int size) {
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