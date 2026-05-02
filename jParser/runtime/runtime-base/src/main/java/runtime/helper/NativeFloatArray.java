package runtime.helper;

public class NativeFloatArray extends NativeArray {


    public static final NativeFloatArray NULL = native_new();

    /**
     * @return An empty instance without a native address
     */
    public static NativeFloatArray native_new() {
        return new NativeFloatArray((byte) 1, (char) 1);
    }

    protected NativeFloatArray() {}

    protected NativeFloatArray(byte b, char c) {
    }

    public NativeFloatArray(int size) {
    }

    public void copy(float [] array) {
        int length = array.length;
        resize(length);
        for(int i = 0; i < length; i++) {
            float value = array[i];
            setValue(i, value);
        }
    }

    public native void setValue(int index, float value);
    public native float getValue(int index);
}
