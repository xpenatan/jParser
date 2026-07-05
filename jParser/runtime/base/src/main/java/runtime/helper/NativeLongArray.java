package runtime.helper;

public class NativeLongArray extends NativeArray {

    public static final NativeLongArray NULL = native_new();

    /**
     * @return An empty instance without a native address
     */
    public static NativeLongArray native_new() {
        return new NativeLongArray((byte) 1, (char) 1);
    }

    protected NativeLongArray(byte b, char c) {}

    protected NativeLongArray() {}

    public NativeLongArray(int size) {
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
