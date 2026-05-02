package runtime.helper;

public class NativeDoubleArray extends NativeArray {

    public static final NativeDoubleArray NULL = native_new();

    /**
     * @return An empty instance without a native address
     */
    public static NativeDoubleArray native_new() {
        return new NativeDoubleArray((byte) 1, (char) 1);
    }

    protected NativeDoubleArray(byte b, char c) {
    }

    protected NativeDoubleArray() {}

    public NativeDoubleArray(int size) {
    }

    public void copy(double [] array) {
        int length = array.length;
        resize(length);
        for(int i = 0; i < length; i++) {
            double value = array[i];
            setValue(i, value);
        }
    }

    public native void setValue(int index, double value);
    public native double getValue(int index);
}
