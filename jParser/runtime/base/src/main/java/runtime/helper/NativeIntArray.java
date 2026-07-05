package runtime.helper;

public class NativeIntArray extends NativeArray {

    public static final NativeIntArray NULL = native_new();

    /**
     * @return An empty instance without a native address
     */
    public static NativeIntArray native_new() {
        return new NativeIntArray((byte) 1, (char) 1);
    }

    protected NativeIntArray() {}

    protected NativeIntArray(byte b, char c) {
    }

    public NativeIntArray(int size) {
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
