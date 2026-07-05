package runtime.helper;

public class NativeBoolArray extends NativeArray {

    public static final NativeBoolArray NULL = native_new();

    /**
     * @return An empty instance without a native address
     */
    public static NativeBoolArray native_new() {
        return new NativeBoolArray((byte) 1, (char) 1);
    }

    protected NativeBoolArray(byte b, char c) {
    }

    protected NativeBoolArray() {}

    public NativeBoolArray(int size) {
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
